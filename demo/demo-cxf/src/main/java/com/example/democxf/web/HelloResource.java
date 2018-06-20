package com.example.democxf.web;


import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
//@Consumes(MediaType.APPLICATION_JSON)
//@Produces(MediaType.APPLICATION_JSON)
public interface HelloResource {

	@POST
	@Path("hello/{name}")
	List<Hello> hello(@PathParam("name") String name, @QueryParam("count") Integer count, @RequestBody Hello body);

}