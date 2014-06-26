package eu.masconsult.template.recipes.net;

import android.content.ContentValues;
import com.robotoworks.mechanoid.db.ContentValuesUtil;
import java.util.Map;

import java.util.List;
public class Recipe {
    
        public static final String KEY_ID = "id";
        public static final String KEY_CATEGORY = "category";
        public static final String KEY_NAME = "name";
        public static final String KEY_IMAGE = "image";
        public static final String KEY_SUMMARY = "summary";
        public static final String KEY_DIRECTIONS = "directions";
        public static final String KEY_PREP_TIME = "prep_time";
        public static final String KEY_COOK_TIME = "cook_time";
        public static final String KEY_TOTAL_TIME = "total_time";
        public static final String KEY_SERVES = "serves";
        public static final String KEY_INGREDIENTS = "ingredients";

	private String id;
	private String category;
	private String name;
	private String image;
	private String summary;
	private String directions;
	private int prepTime;
	private int cookTime;
	private int totalTime;
	private int serves;
	private List<String> ingredients;
	
	public String getId(){
		return id;
	}
	public void setId(String value){
		this.id = value;
	}
	public String getCategory(){
		return category;
	}
	public void setCategory(String value){
		this.category = value;
	}
	public String getName(){
		return name;
	}
	public void setName(String value){
		this.name = value;
	}
	public String getImage(){
		return image;
	}
	public void setImage(String value){
		this.image = value;
	}
	public String getSummary(){
		return summary;
	}
	public void setSummary(String value){
		this.summary = value;
	}
	public String getDirections(){
		return directions;
	}
	public void setDirections(String value){
		this.directions = value;
	}
	public int getPrepTime(){
		return prepTime;
	}
	public void setPrepTime(int value){
		this.prepTime = value;
	}
	public int getCookTime(){
		return cookTime;
	}
	public void setCookTime(int value){
		this.cookTime = value;
	}
	public int getTotalTime(){
		return totalTime;
	}
	public void setTotalTime(int value){
		this.totalTime = value;
	}
	public int getServes(){
		return serves;
	}
	public void setServes(int value){
		this.serves = value;
	}
	public List<String> getIngredients(){
		return ingredients;
	}
	public void setIngredients(List<String> value){
		this.ingredients = value;
	}
	
	public ContentValues toContentValues() {
	    return toContentValues(null);
	}
	
	public ContentValues toContentValues(Map<String, String> map) {
	    ContentValues values = new ContentValues();
	    
            ContentValuesUtil.putMapped(KEY_ID, map, values, id);
            ContentValuesUtil.putMapped(KEY_CATEGORY, map, values, category);
            ContentValuesUtil.putMapped(KEY_NAME, map, values, name);
            ContentValuesUtil.putMapped(KEY_IMAGE, map, values, image);
            ContentValuesUtil.putMapped(KEY_SUMMARY, map, values, summary);
            ContentValuesUtil.putMapped(KEY_DIRECTIONS, map, values, directions);
            ContentValuesUtil.putMapped(KEY_PREP_TIME, map, values, prepTime);
            ContentValuesUtil.putMapped(KEY_COOK_TIME, map, values, cookTime);
            ContentValuesUtil.putMapped(KEY_TOTAL_TIME, map, values, totalTime);
            ContentValuesUtil.putMapped(KEY_SERVES, map, values, serves);
            ContentValuesUtil.putMapped(KEY_INGREDIENTS, map, values, ingredients);

        return values;
	}
}
