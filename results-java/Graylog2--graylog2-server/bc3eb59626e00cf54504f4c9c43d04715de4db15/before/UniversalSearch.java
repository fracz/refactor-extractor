package models;

import java.io.IOException;
import java.net.URL;

import lib.APIException;
import lib.Api;
import models.api.responses.DateHistogramResponse;
import models.api.responses.SearchResultResponse;
import models.api.results.DateHistogramResult;
import models.api.results.SearchResult;

public class UniversalSearch {

	private final String query;
	private final int timerange;

	public UniversalSearch(String query, int timerange) {
		this.query = Api.urlEncode(query);
		this.timerange = timerange;
	}

	public SearchResult search() throws IOException, APIException {
		URL url = Api.buildTarget("search/universal?query=" + query + "&timerange=" + timerange);

		SearchResultResponse response = Api.get(url, new SearchResultResponse());
		SearchResult result = new SearchResult(
				response.query,
				response.total_results,
				response.time,
				response.messages,
				response.fields
		);

		return result;
	}

	public DateHistogramResult dateHistogram(String interval) throws IOException, APIException {
		String i = Api.urlEncode(interval);
		URL url = Api.buildTarget("search/universal/histogram?interval=" + i + "&query=" + query + "&timerange=" + timerange);

		DateHistogramResponse response = Api.get(url, new DateHistogramResponse());
		return new DateHistogramResult(response.query, response.time, response.interval, response.results);
	}

}