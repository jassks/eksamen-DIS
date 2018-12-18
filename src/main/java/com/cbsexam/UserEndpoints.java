package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON (FIX)
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    // The json object gets encrypted
    json = Encryption.encryptDecryptXOR(json);


    // TODO: What should happen if something breaks down? (FIX)

      // There is already a try-catch in the userController, so it doesn't make sense to use one here.
      // But instead it could return status 400, so the user knows that something went wrong
    if (user != null) {
        // Return the user with the status code 200
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
        // If the user is not found, a message is returned with the status code 400
        return Response.status(400).entity("Could not find user").build();
    }
  }


  public static UserCache  userCache = new UserCache();

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = userCache.getUser(false);

    // TODO: Add Encryption to JSON (FIX)
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    //The json object gets encrypted
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system (FIX)
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

      //Read the json from body and transfer it to a user class
      User user = new Gson().fromJson(body, User.class);

      //Get a token back by using the login method in user controller using the user above
      String token = UserController.loginUser(user);

      if (token != ""){
          // Return a response with status 200 and JSON as type
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Here is your token: " + token).build();
      } else {
          // Status code 400 is returned if the user doesn't exist in the system
          return Response.status(400).entity("Could not verify user").build();
      }

  }

  // TODO: Make the system able to delete users (FIX)
  @DELETE
  @Path("/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("token") String token) {

      // The delete user method from user controller is called with the token, extended from the path, as parameter
      Boolean deleted = UserController.deleteUser(token);

      if(deleted) {
          // Return a response with status 200 and JSON as type
          return Response.status(200).entity("User deleted").build();
      } else {
          // If the delete user method return boolean "false", status code 400 is returned
          return Response.status(400).entity("Could not delete user").build();
      }
  }


  // TODO: Make the system able to update users (FIX)
  @PUT
  @Path("/{idUser}/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("token") String token, String body) {

    // Read the json from body and transfer it to a user class
    User user = new Gson().fromJson(body, User.class);

    /*
    The update user method from the user controller is called with following parameters:
    (1) The json object user
    (2) The token extended from the path
     */
    Boolean updated = UserController.updateUser(user,token);

    if (updated){
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User has been updated").build();
    }  else {
        // If the update user method return boolean "false", status code 400 is returned
      return Response.status(400).entity("Could not update user").build();
    }
  }
}
