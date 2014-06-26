package eu.masconsult.template.recipes.net;

import com.robotoworks.mechanoid.net.JsonEntityReader;
import com.robotoworks.mechanoid.net.JsonEntityReaderProvider;
import java.io.IOException;
import com.robotoworks.mechanoid.internal.util.JsonReader;
import com.robotoworks.mechanoid.internal.util.JsonToken;
import java.util.List;
import com.robotoworks.mechanoid.internal.util.JsonUtil;

public class RecipeReader extends JsonEntityReader<Recipe> {			
	
	public RecipeReader(JsonEntityReaderProvider provider) {
		super(provider);
	}
	
	public void read(JsonReader reader, Recipe entity) throws IOException {
		reader.beginObject();
		
		while(reader.hasNext()) {
			String name = reader.nextName();
			
			if(reader.peek() == JsonToken.NULL) {
				reader.skipValue();
				continue;
			}
			
			if(name.equals("id")) {
				entity.setId(reader.nextString());
			}
			else if(name.equals("category")) {
				entity.setCategory(reader.nextString());
			}
			else if(name.equals("name")) {
				entity.setName(reader.nextString());
			}
			else if(name.equals("image")) {
				entity.setImage(reader.nextString());
			}
			else if(name.equals("summary")) {
				entity.setSummary(reader.nextString());
			}
			else if(name.equals("directions")) {
				entity.setDirections(reader.nextString());
			}
			else if(name.equals("prep_time")) {
				entity.setPrepTime(reader.nextInt());
			}
			else if(name.equals("cook_time")) {
				entity.setCookTime(reader.nextInt());
			}
			else if(name.equals("total_time")) {
				entity.setTotalTime(reader.nextInt());
			}
			else if(name.equals("serves")) {
				entity.setServes(reader.nextInt());
			}
			else if(name.equals("ingredients")) {
				List<String> entityMember = JsonUtil.readStringList(reader);
				entity.setIngredients(entityMember);
			}
			else {
				reader.skipValue();
			}
		}
		
		reader.endObject();
	}
	
	public void readList(JsonReader reader, List<Recipe> entities) throws IOException {
		reader.beginArray();
		
		while(reader.hasNext()) {
			Recipe item = new Recipe();
			read(reader, item);
			entities.add(item);
			
		}
		
		reader.endArray();
	}
}
