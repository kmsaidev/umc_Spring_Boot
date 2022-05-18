package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public GetUserInfoRes selectUserInfo(int userIdx){
        String selectUserInfoQuery = "SELECT u.userIdx as userIdx,\n" +
                "u.nickName as nickName,\n" +
                "u.name as name,\n" +
                "u.profileImgUrl as profileImgUrl,\n" +
                "u.website as website,\n" +
                "u.introduction as introduction,\n" +
                "if(postCount is null, 0, postCount) as postCount,\n" +
                "if(followerCount is null, 0, followerCount) as followerCount,\n" +
                "if(followingCount is null, 0, followingCount) as followingCount\n" +
                "FROM User as u\n" +
                "left join (select userIdx, count(postIdx) as postCount from Post WHERE status = 'ACTIVE' group by userIdx) p on p.userIdx = u.userIdx\n" +
                "left join (select followerIdx, count(followIdx) as followerCount from Follow WHERE status = 'ACTIVE' group by followerIdx) fc on fc.followerIdx = u.userIdx\n" +
                "left join (select followeeIdx, count(followIdx) as followingCount from Follow WHERE status = 'ACTIVE' group by followeeIdx) f on f.followeeIdx = u.userIdx\n" +
                "WHERE u.userIdx = ? and u.status = 'ACTIVE'";
        int selectUserInfoParam = userIdx;
        return this.jdbcTemplate.queryForObject(selectUserInfoQuery,
                (rs,rowNum) -> new GetUserInfoRes(
                        rs.getString("nickName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("website"),
                        rs.getString("introduction"),
                        rs.getInt("followerCount"),
                        rs.getInt("followingCount"),
                        rs.getInt("postCount")
                ), selectUserInfoParam);
    }

    public List<GetUserPostsRes> selectUserPosts(int userIdx){
        String selectUserPostsQuery = "SELECT p.postIdx as postIdx,\n" +
                "pi.imgUrl as postImgUrl\n" +
                "FROM Post as p\n" +
                "join PostImgUrl as pi on pi.postIdx = p.postIdx and pi.status = 'ACTIVE'\n" +
                "join User as u on u.userIdx = p.userIdx\n" +
                "WHERE p.status = 'ACTIVE' and u.userIdx = ?\n" +
                "group by p.postIdx\n" +
                "HAVING min(pi.postImgUrlIdx)\n" +
                "order by p.postIdx;";
        int selectUserPostsParam = userIdx;
        return this.jdbcTemplate.query(selectUserPostsQuery,
                (rs,rowNum) -> new GetUserPostsRes(
                        rs.getInt("postIdx"),
                        rs.getString("postImgUrl")
                ), selectUserPostsParam);
    }

//    public GetUserFeedRes getUsersByEmail(String email){
//        String getUsersByEmailQuery = "select userIdx,name,nickName,email from User where email=?";
//        String getUsersByEmailParams = email;
//        return this.jdbcTemplate.queryForObject(getUsersByEmailQuery,
//                (rs, rowNum) -> new GetUserFeedRes(
//                        rs.getInt("userIdx"),
//                        rs.getString("name"),
//                        rs.getString("nickName"),
//                        rs.getString("email")),
//                getUsersByEmailParams);
//    }
//
//
//    public GetUserFeedRes getUsersByIdx(int userIdx){
//        String getUsersByIdxQuery = "select userIdx,name,nickName,email from User where userIdx=?";
//        int getUsersByIdxParams = userIdx;
//        return this.jdbcTemplate.queryForObject(getUsersByIdxQuery,
//                (rs, rowNum) -> new GetUserFeedRes(
//                        rs.getInt("userIdx"),
//                        rs.getString("name"),
//                        rs.getString("nickName"),
//                        rs.getString("email")),
//                getUsersByIdxParams);
//    }

    public DeleteUserRes deleteUser(DeleteUserReq deleteUserReq){
        int userIdx = deleteUserReq.getUserIdx();
        int deleteUserParams = userIdx;
        String selectUserQuery = "select name, nickName, email, phone, pwd from User where userIdx=?";
        DeleteUserRes deleteUserRes = this.jdbcTemplate.queryForObject(selectUserQuery,
                (rs, rowNum) -> new DeleteUserRes(
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("pwd")),
                deleteUserParams);
        String deleteUserQuery = "delete from User where userIdx=?";
        this.jdbcTemplate.update(deleteUserQuery, deleteUserParams);
        return deleteUserRes;
    }

    public int createUser(PostUserReq postUserReq){
        String createUserQuery = "insert into User (name, nickName, phone, email, pwd) VALUES (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getName(), postUserReq.getNickName(), postUserReq.getPhone(), postUserReq.getEmail(), postUserReq.getPwd()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInsertIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdQuery,int.class);
    }

    public int checkEmail(String email){
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    public int modifyUserName(PatchUserReq patchUserReq){
        String modifyUserNameQuery = "update User set nickName = ? where userIdx = ? ";
        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickName(), patchUserReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserNameQuery,modifyUserNameParams);
    }




}
