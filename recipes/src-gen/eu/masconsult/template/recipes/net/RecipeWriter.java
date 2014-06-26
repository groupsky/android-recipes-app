package eu.masconsult.template.recipes.net;

import com.robotoworks.mechanoid.net.JsonEntityWriter;
import com.robotoworks.mechanoid.net.JsonEntityWriterProvider;
import java.io.IOException;
import com.robotoworks.mechanoid.internal.util.JsonWriter;
import java.util.List;
import com.robotoworks.mechanoid.internal.util.JsonUtil;


public class RecipeWriter extends JsonEntityWriter<Recipe> {

	public RecipeWriter(JsonEntityWriterProvider provider) {
		super(provider);
	}
	
	public void write(JsonWriter writer, Recipe entity) throws IOException {
		writer.beginObject();
		
		writer.name("id");
		writer.value(entity.getId());
		writer.name("category");
		writer.value(entity.getCategory());
		writer.name("name");
		writer.value(entity.getName());
		writer.name("image");
		writer.value(entity.getImage());
		writer.name("summary");
		writer.value(entity.getSummary());
		writer.name("directions");
		writer.value(entity.getDirections());
		writer.name("prep_time");
		writer.value(entity.getPrepTime());
		writer.name("cook_time");
		writer.value(entity.getCookTime());
		writer.name("total_time");
		writer.value(entity.getTotalTime());
		writer.name("serves");
		writer.value(entity.getServes());
		if(entity.getIngredients() != null) {
			writer.name("ingredients");
			JsonUtil.writeStringList(writer, entity.getIngredients());
		}
		
		writer.endObject();
	}
	
	public void writeList(JsonWriter writer, List<Recipe> entities) throws IOException {
		writer.beginArray();
		
		for(Recipe item:entities) {
			write(writer, item);
		}
		
		writer.endArray();
	}
}
