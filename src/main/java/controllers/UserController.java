package controllers;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cbsexam.UserEndpoints;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getLong("created_at"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                    rs.getString("email"),
                    rs.getLong("created_at"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    Hashing hashing = new Hashing();

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    long createdTime = System.currentTimeMillis() / 1000L;

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it (FIX)
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + hashing.hashWithSaltSha(user.getPassword())
            + "', '"
            + user.getEmail()
            + "', "
            + createdTime
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    UserEndpoints.userCache.getUser(true);

    // Return user
    return user;
  }

  public static Boolean updateUser(User user, String token) {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

      try{
          //Decodes the jwt token to get the user id
          DecodedJWT jwt = JWT.decode(token);
          int id = jwt.getClaim("userId").asInt();

          try{
              // Uses a prepared statement to update the user's new information in the database
              PreparedStatement updateUser = dbCon.getConnection().prepareStatement("UPDATE user SET " +
                      "first_name = ?, last_name = ?, password = ?, email = ? WHERE id=? ");

              updateUser.setString(1, user.getFirstname());
              updateUser.setString(2, user.getLastname());
              updateUser.setString(3, Hashing.hashWithSaltSha(user.getPassword()));
              updateUser.setString(4, user.getEmail());
              updateUser.setInt(5, id);

              int rowsAffected = updateUser.executeUpdate();

              if (rowsAffected == 1){

                  //Updates the user cache, so the user is also updated in the cache
                  UserEndpoints.userCache.getUser(true);

                  return true;
              }

          } catch (SQLException sql){
              sql.printStackTrace();;
          }

      } catch (JWTDecodeException ex) {
          ex.printStackTrace();
      }

    return false;

  }


  public static Boolean deleteUser (String token) {

    if(dbCon == null){
      dbCon = new DatabaseController();
    }

    try{
        // Decodes the jwt token to get the user id
        DecodedJWT jwt = JWT.decode(token);
        int id = jwt.getClaim("userId").asInt();

        try{
            // Uses preparestatement to delete the user in the database
            PreparedStatement deleteUser = dbCon.getConnection().prepareStatement("DELETE FROM user WHERE id = ? ");

            deleteUser.setInt(1, id);

            int rowsAffected = deleteUser.executeUpdate();

            if (rowsAffected == 1){

                // Updates the user cache, so the user is also deleted in the cache
                UserEndpoints.userCache.getUser(true);
                return true;
            }

        } catch (SQLException sql){
            sql.printStackTrace();

        }

    } catch (JWTDecodeException ex) {
        ex.printStackTrace();
    }

    return false;
  }


  public static String loginUser (User user) {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    ResultSet resultSet;
    User newUser;
    String token = null;


    try{
        // Uses prepared statement to if there is any user in the database with the specific email and password
        PreparedStatement loginUser = dbCon.getConnection().prepareStatement("SELECT * FROM user WHERE email = ? AND password = ?");

        loginUser.setString(1,user.getEmail());
        loginUser.setString(2,Hashing.hashWithSaltSha(user.getPassword()));

        resultSet = loginUser.executeQuery();

        //If there is found any user --> A new user object is created with the informations from the database
        if (resultSet.next()){
            newUser = new User (
                    resultSet.getInt("id"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));

            if (newUser != null){
                try{
                    //Assigns a token to the user
                    Algorithm algorithm = Algorithm.HMAC256("secret");
                    token = JWT.create()
                            .withClaim("userId", newUser.getId())
                            .withIssuer("auth0")
                            .sign(algorithm);
                }catch(JWTCreationException ex){

                } finally {

                    //Returns token
                    return token;
                }

            }
        } else {
            System.out.print("No user found");
        }

    } catch (SQLException sqlEx) {
      sqlEx.printStackTrace();
    }

    return "";

  }


}
