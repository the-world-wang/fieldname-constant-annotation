package com.lisb.defname.internal;

import com.lisb.defname.DefineNames;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Modifier;

class DefineNameClassWriter {

	private static final String GENERATE_CLASS_PREFIX = "_C";
	private static final String GENERATE_CLASS_SUFFIX = "";

	private final String packageName;
	private final String targetClassSimpleName;
	private final DefineNames.Case[] cases;
	private final Set<String> fields = new HashSet<String>();

	DefineNameClassWriter(final String packageName,
			final String targetClassSimpleName, final DefineNames.Case[] cases) {
		this.packageName = packageName;
		this.targetClassSimpleName = targetClassSimpleName;
		this.cases = cases;
	}

	void addFields(final String field) {
		for (final DefineNames.Case c : cases) {
			final String fieldName = c == DefineNames.Case.Original ? field
					: convertToSnakeCase(field);
			fields.add(fieldName);
		}
	}

	String getClassFQDN() {
		return packageName + "." + GENERATE_CLASS_PREFIX
				+ targetClassSimpleName + GENERATE_CLASS_SUFFIX;
	}

	void write(final Writer writer) throws IOException {
		JavaWriter javaWriter = null;
		try {
			javaWriter = new JavaWriter(writer);
			javaWriter.emitPackage(packageName).beginType(getClassFQDN(),
					"class", EnumSet.of(Modifier.PUBLIC));

			// フィールドの並び順が予測可能なようにソートする
			final List<String> fields = new ArrayList<String>(this.fields);
			Collections.sort(fields);

			for (final String field : fields) {
				javaWriter.emitField("String", field, EnumSet.of(
						Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\""
						+ field + "\"");
			}
			javaWriter.endType();
		} finally {
			if (javaWriter != null) {
				try {
					javaWriter.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");

	static final String convertToSnakeCase(final String original) {
		final StringBuffer sb = new StringBuffer();
		final Matcher m = UPPERCASE.matcher(original);
		while (m.find()) {
			if (m.start() != 0) {
				m.appendReplacement(sb, "_");
			} else {
				m.appendReplacement(sb, "");
			}
			sb.append(m.group().toLowerCase());
		}
		m.appendTail(sb);
		return sb.toString();
	}
}