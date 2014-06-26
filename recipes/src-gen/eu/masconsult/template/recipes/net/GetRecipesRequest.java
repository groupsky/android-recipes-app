package eu.masconsult.template.recipes.net;


import android.net.Uri;
import com.robotoworks.mechanoid.net.ServiceRequest;

public class GetRecipesRequest extends ServiceRequest {
	
	private static final String PATH = "";
	
	public GetRecipesRequest(){
	}
	
	@Override
	public String createUrl(String baseUrl){
		Uri.Builder uriBuilder = Uri.parse(baseUrl + PATH).buildUpon();
			
		return uriBuilder.toString();			
	}
}
