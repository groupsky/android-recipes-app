package eu.masconsult.template.recipes.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.robotoworks.mechanoid.internal.util.JsonReader;
import com.robotoworks.mechanoid.internal.util.JsonToken;
import com.robotoworks.mechanoid.net.JsonEntityReaderProvider;
import com.robotoworks.mechanoid.util.Closeables;

public class CustomGetRecipesResult extends GetRecipesResult {

	public CustomGetRecipesResult(JsonEntityReaderProvider provider, InputStream inStream)
			throws IOException {
		super(null, null);

		JsonReader reader = null;
		try {
			if (inStream != null) {
				reader = new JsonReader(new InputStreamReader(inStream, Charset.defaultCharset()));
				GetRecipesResult subject = this;

				if (JsonToken.BEGIN_ARRAY.equals(reader.peek())) {
					List<Recipe> entityMember = new ArrayList<Recipe>();
					provider.get(Recipe.class).readList(reader, entityMember);
					subject.setRecipes(entityMember);
				} else {
					reader.beginObject();

					while (reader.hasNext()) {
						String name = reader.nextName();

						if (reader.peek() == JsonToken.NULL) {
							reader.skipValue();
							continue;
						}

						if (name.equals("next_update")) {
							subject.setNextUpdate(reader.nextString());
						} else if (name.equals("recipes")) {
							List<Recipe> entityMember = new ArrayList<Recipe>();
							provider.get(Recipe.class).readList(reader, entityMember);
							subject.setRecipes(entityMember);
						} else if (name.equals("next_page")) {
							subject.setNextPage(reader.nextString());
						} else {
							reader.skipValue();
						}
					}

					reader.endObject();
				}
			}
		} finally {
			Closeables.closeSilently(reader);
		}
	}
}
