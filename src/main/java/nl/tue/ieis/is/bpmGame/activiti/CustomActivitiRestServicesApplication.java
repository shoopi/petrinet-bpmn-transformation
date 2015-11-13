package main.java.nl.tue.ieis.is.bpmGame.activiti;

import org.activiti.rest.common.api.DefaultResource;
import org.activiti.rest.common.filter.JsonpFilter;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.activiti.rest.service.application.RestServicesInit;
import org.restlet.Restlet;
import org.restlet.routing.Router;


public class CustomActivitiRestServicesApplication extends ActivitiRestServicesApplication {
	@Override
	public synchronized Restlet createInboundRoot() {
		
		initializeAuthentication();
	    
	    Router router = new Router(getContext());
	    router.attachDefault(DefaultResource.class);
	    RestServicesInit.attachResources(router);
	    
	    //add GET Service own router
	   // router.attach("/runtime/tasklist/{processInstanceId}", TaskList.class);

	    JsonpFilter jsonpFilter = new JsonpFilter(getContext());
	    authenticator.setNext(jsonpFilter);
	    jsonpFilter.setNext(router);

	    return authenticator;
    }

}
