package sample;

public interface UserQueries {
     String UPDATE_USER_QUERY = "insert into user1 (username,password,vector) values(?,?,?)";
     String SELECT_ALL_USERS = "select u.* from user1 u";
     String SELECT_USER_BY_ID = "select u.* from user1 u where u.id = ?";
}
