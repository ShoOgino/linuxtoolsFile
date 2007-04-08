package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

public class SpecfileScanner extends RuleBasedScanner {

	private static String[] sections = { "%prep", "%build", "%install", "%pre",
			"%preun", "%post", "%files", "%changelog", "%package",
			"%description", "%postun" };

	private static String[] definedMacros = { "%define", "%make", "%setup",
			"%attrib", "%defattr", "%attr", "%dir", "%config", "%docdir",
			"%doc", "%lang", "%verify", "%ghost" };

	private static String[] keywords = { "%if", "%ifarch", "%ifnarch", "%else",
			"%endif" };

	private static String[] tags = { "Summary", "Name", "Version",
			"Packager", "Icon", "URL", "Prefix", "Packager",
			"Group", "License", "Release", "BuildRoot", "Distribution",
			"Vendor", "Provides", "ExclusiveArch", "ExcludeArch",
			"ExclusiveOS", "BuildArch", "BuildArchitectures",
			"AutoRequires", "AutoReq", "AutoReqProv", "AutoProv", "Epoch",
			"ExcludeOS" };

	public SpecfileScanner(ColorManager manager) {
		IToken sectionToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.SECTIONS), null, SWT.ITALIC));

		IToken macroToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.MACROS)));

		IToken keywordToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.KEYWORDS), null, SWT.BOLD));

		IToken tagToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.TAGS)));

		IToken commentToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.COMMENT)));

		List rules = new ArrayList();

		// Comments
		rules.add(new EndOfLineRule("#", commentToken));

		// %{ .... }
		rules.add(new SingleLineRule("%{", "}", macroToken));

		// %define, %make, ...
		WordRule wordRule = new WordRule(new MacroWordDetector(),
				Token.UNDEFINED);
		for (int i = 0; i < definedMacros.length; i++)
			wordRule.addWord(definedMacros[i], macroToken);
		rules.add(wordRule);

		// %patch[0-9]+[\ \t]
		rules.add(new StringWithEndingRule("%patch", new PatchNumberDetector(),
				macroToken, true));

		// %if, %else ...
		wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
		for (int i = 0; i < keywords.length; i++)
			wordRule.addWord(keywords[i], keywordToken);
		rules.add(wordRule);

		// %prep, %build, ...
		wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
		for (int i = 0; i < sections.length; i++)
			wordRule.addWord(sections[i], sectionToken);
		rules.add(wordRule);

		// Name:, Summary:, ...
		wordRule = new WordRule(new TagWordDetector(), Token.UNDEFINED);
		for (int i = 0; i < tags.length; i++)
			wordRule.addWord(tags[i] + ":", tagToken);
		rules.add(wordRule);

		// Source[0-9]*:, Patch[0-9]*:
		rules.add(new StringWithEndingRule("Source",
				new SuffixNumberDetector(), tagToken, false));
		rules.add(new StringWithEndingRule("Patch", new SuffixNumberDetector(),
				tagToken, false));

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
