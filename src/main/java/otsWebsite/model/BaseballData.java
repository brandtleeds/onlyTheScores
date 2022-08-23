package otsWebsite.model;

public class BaseballData {

    String get;
    Object parameters;
    String[] errors;
    int results;
    Object[] response;

    public BaseballData(String get, Object parameters, String[] errors, int results, Object[] response) {
        this.get = get;
        this.parameters = parameters;
        this.errors = errors;
        this.results = results;
        this.response = response;
    }

    public BaseballData() {
    }

    public String getGet() {
        return get;
    }

    public void setGet(String get) {
        this.get = get;
    }

    public Object getParameters() {
        return parameters;
    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public String[] getErrors() {
        return errors;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public Object[] getResponse() {
        return response;
    }

    public void setResponse(Object[] response) {
        this.response = response;
    }
}
