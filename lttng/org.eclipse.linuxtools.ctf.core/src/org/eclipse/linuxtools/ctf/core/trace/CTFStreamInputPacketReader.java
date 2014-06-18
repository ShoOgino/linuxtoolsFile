/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.ctf.core.trace;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.CTFStrings;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

import com.google.common.collect.ImmutableList;

/**
 * CTF trace packet reader. Reads the events of a packet of a trace file.
 *
 * @author Matthew Khouzam
 * @author Simon Marchi
 * @since 3.0
 */
public class CTFStreamInputPacketReader implements IDefinitionScope, AutoCloseable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** BitBuffer used to read the trace file. */
    @Nullable
    private BitBuffer fBitBuffer;

    /** StreamInputReader that uses this StreamInputPacketReader. */
    private final CTFStreamInputReader fStreamInputReader;

    /** Trace packet header. */
    private final StructDeclaration fTracePacketHeaderDecl;

    /** Stream packet context definition. */
    private final StructDeclaration fStreamPacketContextDecl;

    /** Stream event header definition. */
    private final StructDeclaration fStreamEventHeaderDecl;

    /** Stream event context definition. */
    private final StructDeclaration fStreamEventContextDecl;

    private StructDefinition fCurrentTracePacketHeaderDef;
    private StructDefinition fCurrentStreamEventHeaderDef;
    private Definition fCurrentStreamPacketContextDef;
    /** Reference to the index entry of the current packet. */
    private StreamInputPacketIndexEntry fCurrentPacket = null;

    /**
     * Last timestamp recorded.
     *
     * Needed to calculate the complete timestamp values for the events with
     * compact headers.
     */
    private long fLastTimestamp = 0;

    /** CPU id of current packet. */
    private int fCurrentCpu = 0;

    private int fLostEventsInThisPacket;

    private long fLostEventsDuration;

    private boolean fHasLost = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputPacketReader.
     *
     * @param streamInputReader
     *            The StreamInputReader to which this packet reader belongs to.
     */
    public CTFStreamInputPacketReader(CTFStreamInputReader streamInputReader) {
        fStreamInputReader = streamInputReader;

        /* Set the BitBuffer's byte order. */
        fBitBuffer = new BitBuffer();
        fBitBuffer.setByteOrder(streamInputReader.getByteOrder());

        final CTFStream currentStream = streamInputReader.getStreamInput().getStream();
        fTracePacketHeaderDecl = currentStream.getTrace().getPacketHeader();
        fStreamPacketContextDecl = currentStream.getPacketContextDecl();
        fStreamEventHeaderDecl = currentStream.getEventHeaderDecl();
        fStreamEventContextDecl = currentStream.getEventContextDecl();
    }

    /**
     * Get the event context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    public StructDefinition getEventContextDefinition(@NonNull BitBuffer input) throws CTFReaderException {
        return fStreamEventContextDecl.createDefinition(this, LexicalScope.STREAM_EVENT_CONTEXT.getName(), input);
    }

    /**
     * Get the stream context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    public StructDefinition getStreamEventHeaderDefinition(@NonNull BitBuffer input) throws CTFReaderException {
        return fStreamEventHeaderDecl.createDefinition(this, LexicalScope.STREAM_EVENT_HEADER.getName(), input);
    }

    /**
     * Get the packet context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    public StructDefinition getStreamPacketContextDefinition(@NonNull BitBuffer input) throws CTFReaderException {
        return fStreamPacketContextDecl.createDefinition(this, LexicalScope.STREAM_PACKET_CONTEXT.getName(), input);
    }

    /**
     * Get the event header defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an header definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    public StructDefinition getTracePacketHeaderDefinition(@NonNull BitBuffer input) throws CTFReaderException {
        return fTracePacketHeaderDecl.createDefinition(this, LexicalScope.TRACE_PACKET_HEADER.getName(), input);
    }

    /**
     * Dispose the StreamInputPacketReader
     */
    @Override
    public void close() {
        fBitBuffer = null;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the current packet
     *
     * @return the current packet
     */
    StreamInputPacketIndexEntry getCurrentPacket() {
        return fCurrentPacket;
    }

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    public int getCPU() {
        return fCurrentCpu;
    }

    @Override
    public LexicalScope getScopePath() {
        return LexicalScope.PACKET;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Changes the current packet to the given one.
     *
     * @param currentPacket
     *            The index entry of the packet to switch to.
     * @throws CTFReaderException
     *             If we get an error reading the packet
     */
    void setCurrentPacket(StreamInputPacketIndexEntry currentPacket) throws CTFReaderException {
        StreamInputPacketIndexEntry prevPacket = null;
        fCurrentPacket = currentPacket;

        if (fCurrentPacket != null) {
            /*
             * Change the map of the BitBuffer.
             */
            ByteBuffer bb = null;
            try {
                bb = fStreamInputReader.getStreamInput().getByteBufferAt(
                        fCurrentPacket.getOffsetBytes(),
                        (fCurrentPacket.getPacketSizeBits() + 7) / 8);
            } catch (IOException e) {
                throw new CTFReaderException(e.getMessage(), e);
            }

            BitBuffer bitBuffer = new BitBuffer(bb);
            fBitBuffer = bitBuffer;
            /*
             * Read trace packet header.
             */
            if (fTracePacketHeaderDecl != null) {
                fCurrentTracePacketHeaderDef = getTracePacketHeaderDefinition(bitBuffer);
            }

            /*
             * Read stream packet context.
             */
            if (fStreamPacketContextDecl != null) {
                fCurrentStreamPacketContextDef = getStreamPacketContextDefinition(bitBuffer);

                /* Read CPU ID */
                if (getCurrentPacket().getTarget() != null) {
                    fCurrentCpu = (int) getCurrentPacket().getTargetId();
                }

                /* Read number of lost events */
                fLostEventsInThisPacket = (int) getCurrentPacket().getLostEvents();
                if (fLostEventsInThisPacket != 0) {
                    fHasLost = true;
                    /*
                     * Compute the duration of the lost event time range. If the
                     * current packet is the first packet, duration will be set
                     * to 1.
                     */
                    long lostEventsStartTime;
                    int index = fStreamInputReader.getStreamInput().getIndex().getEntries().indexOf(currentPacket);
                    if (index == 0) {
                        lostEventsStartTime = currentPacket.getTimestampBegin() + 1;
                    } else {
                        prevPacket = fStreamInputReader.getStreamInput().getIndex().getEntries().get(index - 1);
                        lostEventsStartTime = prevPacket.getTimestampEnd();
                    }
                    fLostEventsDuration = Math.abs(lostEventsStartTime - currentPacket.getTimestampBegin());
                }
            }

            /*
             * Use the timestamp begin of the packet as the reference for the
             * timestamp reconstitution.
             */
            fLastTimestamp = currentPacket.getTimestampBegin();
        } else {
            fBitBuffer = null;
            fLastTimestamp = 0;
        }
    }

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    public boolean hasMoreEvents() {
        BitBuffer bitBuffer = fBitBuffer;
        StreamInputPacketIndexEntry currentPacket = fCurrentPacket;
        if (currentPacket != null && bitBuffer != null) {
            return fHasLost || (bitBuffer.position() < currentPacket.getContentSizeBits());
        }
        return false;
    }

    /**
     * Reads the next event of the packet into the right event definition.
     *
     * @return The event definition containing the event data that was just
     *         read.
     * @throws CTFReaderException
     *             If there was a problem reading the trace
     */
    public EventDefinition readNextEvent() throws CTFReaderException {
        /* Default values for those fields */
        long eventID = EventDeclaration.UNSET_EVENT_ID;
        long timestamp = 0;
        if (fHasLost) {
            fHasLost = false;
            EventDeclaration lostEventDeclaration = EventDeclaration.getLostEventDeclaration();
            StructDeclaration lostFields = lostEventDeclaration.getFields();
            // this is a hard coded map, we know it's not null
            IntegerDeclaration lostFieldsDecl = (IntegerDeclaration) lostFields.getFields().get(CTFStrings.LOST_EVENTS_FIELD);
            if (lostFieldsDecl == null)
            {
                throw new IllegalStateException("Lost events count not declared!"); //$NON-NLS-1$
            }
            IntegerDeclaration lostEventsDurationDecl = (IntegerDeclaration) lostFields.getFields().get(CTFStrings.LOST_EVENTS_DURATION);
            if (lostEventsDurationDecl == null) {
                throw new IllegalStateException("Lost events duration not declared!"); //$NON-NLS-1$
            }
            IntegerDefinition lostDurationDef = new IntegerDefinition(lostFieldsDecl, null, CTFStrings.LOST_EVENTS_DURATION, fLostEventsDuration);
            IntegerDefinition lostCountDef = new IntegerDefinition(lostEventsDurationDecl, null, CTFStrings.LOST_EVENTS_FIELD, fLostEventsInThisPacket);
            IntegerDefinition[] fields = new IntegerDefinition[] { lostCountDef, lostDurationDef };
            /* this is weird notation, but it's the java notation */
            final ImmutableList<String> fieldNameList = ImmutableList.<String> builder().add(CTFStrings.LOST_EVENTS_FIELD).add(CTFStrings.LOST_EVENTS_DURATION).build();
            return new EventDefinition(
                    lostEventDeclaration,
                    fStreamInputReader,
                    fLastTimestamp,
                    null,
                    null,
                    null,
                    new StructDefinition(
                            lostFields,
                            this, "fields", //$NON-NLS-1$
                            fieldNameList,
                            fields
                    ));

        }

        final BitBuffer currentBitBuffer = fBitBuffer;
        if( currentBitBuffer == null ) {
            return null;
        }
        final long posStart = currentBitBuffer.position();
        /* Read the stream event header. */
        if (fStreamEventHeaderDecl != null) {
            fCurrentStreamEventHeaderDef = getStreamEventHeaderDefinition(currentBitBuffer);

            /* Check for the event id. */
            Definition idDef = fCurrentStreamEventHeaderDef.lookupDefinition("id"); //$NON-NLS-1$
            if (idDef instanceof SimpleDatatypeDefinition) {
                eventID = ((SimpleDatatypeDefinition) idDef).getIntegerValue();
            } else if (idDef != null) {
                throw new CTFReaderException("Incorrect event id : " + eventID); //$NON-NLS-1$
            }

            /*
             * Get the timestamp from the event header (may be overridden later
             * on)
             */
            IntegerDefinition timestampDef = fCurrentStreamEventHeaderDef.lookupInteger("timestamp"); //$NON-NLS-1$
            if (timestampDef != null) {
                timestamp = calculateTimestamp(timestampDef);
            } // else timestamp remains 0

            /* Check for the variant v. */
            Definition variantDef = fCurrentStreamEventHeaderDef.lookupDefinition("v"); //$NON-NLS-1$
            if (variantDef instanceof VariantDefinition) {

                /* Get the variant current field */
                StructDefinition variantCurrentField = (StructDefinition) ((VariantDefinition) variantDef).getCurrentField();

                /*
                 * Try to get the id field in the current field of the variant.
                 * If it is present, it overrides the previously read event id.
                 */
                Definition idIntegerDef = variantCurrentField.lookupDefinition("id"); //$NON-NLS-1$
                if (idIntegerDef instanceof IntegerDefinition) {
                    eventID = ((IntegerDefinition) idIntegerDef).getValue();
                }

                /*
                 * Get the timestamp. This would overwrite any previous
                 * timestamp definition
                 */
                Definition def = variantCurrentField.lookupDefinition("timestamp"); //$NON-NLS-1$
                if (def instanceof IntegerDefinition) {
                    timestamp = calculateTimestamp((IntegerDefinition) def);
                }
            }
        }

        /* Get the right event definition using the event id. */
        IEventDeclaration eventDeclaration = fStreamInputReader.getStreamInput().getStream().getEvents().get(eventID);
        if (eventDeclaration == null) {
            throw new CTFReaderException("Incorrect event id : " + eventID); //$NON-NLS-1$
        }
        EventDefinition eventDef = eventDeclaration.createDefinition(fStreamInputReader, currentBitBuffer, timestamp);

        /*
         * Set the event timestamp using the timestamp calculated by
         * updateTimestamp.
         */

        if (posStart == currentBitBuffer.position()) {
            throw new CTFReaderException("Empty event not allowed, event: " + eventDef.getDeclaration().getName()); //$NON-NLS-1$
        }

        return eventDef;
    }

    /**
     * Calculates the timestamp value of the event, possibly using the timestamp
     * from the last event.
     *
     * @param timestampDef
     *            Integer definition of the timestamp.
     * @return The calculated timestamp value.
     */
    private long calculateTimestamp(IntegerDefinition timestampDef) {
        long newval;
        long majorasbitmask;
        int len = timestampDef.getDeclaration().getLength();

        /*
         * If the timestamp length is 64 bits, it is a full timestamp.
         */
        if (timestampDef.getDeclaration().getLength() == 64) {
            fLastTimestamp = timestampDef.getValue();
            return fLastTimestamp;
        }

        /*
         * Bit mask to keep / remove all old / new bits.
         */
        majorasbitmask = (1L << len) - 1;

        /*
         * If the new value is smaller than the corresponding bits of the last
         * timestamp, we assume an overflow of the compact representation.
         */
        newval = timestampDef.getValue();
        if (newval < (fLastTimestamp & majorasbitmask)) {
            newval = newval + (1L << len);
        }

        /* Keep only the high bits of the old value */
        fLastTimestamp = fLastTimestamp & ~majorasbitmask;

        /* Then add the low bits of the new value */
        fLastTimestamp = fLastTimestamp + newval;

        return fLastTimestamp;
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath.equals(LexicalScope.STREAM_PACKET_CONTEXT)) {
            return fCurrentStreamPacketContextDef;
        }
        if (lookupPath.equals(LexicalScope.TRACE_PACKET_HEADER)) {
            return fCurrentTracePacketHeaderDef;
        }
        return null;
    }

    /**
     * Get stream event header
     *
     * @return the stream event header
     */
    public StructDefinition getCurrentStreamEventHeader() {
        return fCurrentStreamEventHeaderDef;
    }

    /**
     * Get the current packet event header
     *
     * @return the current packet event header
     */
    public StructDefinition getCurrentPacketEventHeader() {
        return fCurrentTracePacketHeaderDef;
    }
}
