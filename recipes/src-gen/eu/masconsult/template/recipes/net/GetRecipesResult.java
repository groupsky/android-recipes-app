package eu.masconsult.template.recipes.net;


import com.robotoworks.mechanoid.net.JsonEntityReaderProvider;
import java.io.IOException;
import com.robotoworks.mechanoid.net.ServiceResult;
import java.io.InputStream;
import com.robotoworks.mechanoid.util.Closeables;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.List;
import com.robotoworks.mechanoid.internal.util.JsonToken;
import com.robotoworks.mechanoid.internal.util.JsonReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class GetRecipesResult extends ServiceResult {
	private List<Recipe> recipes;
	private String nextPage;
	private String nextUpdate;
	public List<Recipe> getRecipes(){
		return this.recipes;
	}
	public void setRecipes(List<Recipe> value){
		this.recipes = value;
	}
	public String getNextPage(){
		return this.nextPage;
	}
	public void setNextPage(String value){
		this.nextPage = value;
	}
	public String getNextUpdate(){
		return this.nextUpdate;
	}
	public void setNextUpdate(String value){
		this.nextUpdate = value;
	}
	
	public GetRecipesResult(JsonEntityReaderProvider provider, InputStream inStream) throws IOException {
		JsonReader reader = null;
		try {
			if(inStream != null) {
				reader = new JsonReader(new BufferedReader(new InputStreamReader(inStream, Charset.defaultCharset())));
		GetRecipesResult subject = this;
		reader.beginObject();
		
		while(reader.hasNext()) {
			String name = reader.nextName();
		
			if(reader.peek() == JsonToken.NULL) {
				reader.skipValue();
				continue;
			}
						
			if(name.equals("next_update")) {
				subject.setNextUpdate(reader.nextString());
			}
			else if(name.equals("recipes")) {
				List<Recipe> entityMember = new ArrayList<Recipe>();
				provider.get(Recipe.class).readList(reader, entityMember);
				subject.setRecipes(entityMember);
			}
			else if(name.equals("next_page")) {
				subject.setNextPage(reader.nextString());
			}
			else {
				reader.skipValue();
			}
		}
		
		reader.endObject();
		}
	} finally {
		Closeables.closeSilently(reader);
	}
	}
}
