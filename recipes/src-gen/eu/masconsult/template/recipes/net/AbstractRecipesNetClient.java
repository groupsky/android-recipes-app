package eu.masconsult.template.recipes.net;


import com.robotoworks.mechanoid.net.Parser;
import java.io.IOException;
import com.robotoworks.mechanoid.net.Response;
import com.robotoworks.mechanoid.net.ServiceException;
import java.io.InputStream;
import com.robotoworks.mechanoid.net.ServiceClient;
import com.robotoworks.mechanoid.net.JsonEntityWriterProvider;
import com.robotoworks.mechanoid.net.JsonEntityReaderProvider;

public abstract class AbstractRecipesNetClient extends ServiceClient {
	private static final String LOG_TAG = "RecipesNetClient";
	
	@Override
	protected String getLogTag() {
		return LOG_TAG;
	}
	
	@Override
	protected JsonEntityWriterProvider createWriterProvider() {
		return new DefaultRecipesNetClientWriterProvider();
	}
	
	@Override
	protected JsonEntityReaderProvider createReaderProvider() {
		return new DefaultRecipesNetClientReaderProvider();
	}
	
	public AbstractRecipesNetClient(String baseUrl, boolean debug){
		super(baseUrl, debug);
		
	}
	
	public Response<GetRecipesResult> getRecipes()
	  throws ServiceException {
	  	return getRecipes(new GetRecipesRequest());
	}
	
	public Response<GetRecipesResult> getRecipes(GetRecipesRequest request)
	  throws ServiceException {
		
		Parser<GetRecipesResult> parser = new Parser<GetRecipesResult>() {
			public GetRecipesResult parse(InputStream inStream) throws IOException {
				return new GetRecipesResult(getReaderProvider(), inStream);
			}
		};
		
		return get(request, parser);
	}
	
}
