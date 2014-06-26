package eu.masconsult.template.recipes.net;

import java.io.IOException;
import java.io.InputStream;

import com.robotoworks.mechanoid.net.Parser;
import com.robotoworks.mechanoid.net.Response;
import com.robotoworks.mechanoid.net.ServiceException;

public class RecipesNetClient extends AbstractRecipesNetClient {

	public RecipesNetClient(String baseUrl) {
		super(baseUrl, false);
	}

	public RecipesNetClient(String baseUrl, boolean debug) {
		super(baseUrl, debug);
	}

	public Response<GetRecipesResult> getRecipes(GetRecipesRequest request, InputStream input)
			throws ServiceException {

		Parser<GetRecipesResult> parser = new Parser<GetRecipesResult>() {
			@Override
			public GetRecipesResult parse(InputStream inStream) throws IOException {
				return new CustomGetRecipesResult(getReaderProvider(), inStream);
			}
		};

		return get(request, parser);
	}
}
