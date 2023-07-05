package cloud.tamacat2.httpd.error;

public class HttpStatusExceptionHandler {

	public static HttpStatusException getException(int code) {
    	switch(code) {
			case 400: return new BadRequestException();
			case 401: return new UnauthorizedException();
			case 403: return new ForbiddenException();
			case 404: return new NotFoundException();
			case 405: return new MethodNotAllowedException();
			case 500: return new InternalServerErrorException();
			case 503: return new ServiceUnavailableException();
			default : return new NotFoundException();
    	}
	}
	
	public static void throwException(int code) throws HttpStatusException {
    	throw getException(code);
	}
}
