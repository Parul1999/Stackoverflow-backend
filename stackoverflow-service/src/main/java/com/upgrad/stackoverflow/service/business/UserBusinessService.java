package com.upgrad.stackoverflow.service.business;

import com.upgrad.stackoverflow.service.common.JwtTokenProvider;
import com.upgrad.stackoverflow.service.dao.UserDao;
import com.upgrad.stackoverflow.service.entity.UserAuthEntity;
import com.upgrad.stackoverflow.service.entity.UserEntity;
import com.upgrad.stackoverflow.service.exception.AuthenticationFailedException;
import com.upgrad.stackoverflow.service.exception.SignOutRestrictedException;
import com.upgrad.stackoverflow.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * The method implements the business logic for signup endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        if(userDao.getUserByUsername(userEntity.getUserName())!=null ||userDao.getUserByEmail(userEntity.getEmail())!=null){
            throw new SignUpRestrictedException("409", "User Already Exist");
        }
        return userDao.createUser(userEntity);
    }

    /**
     * The method implements the business logic for signin endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticate(String username, String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUsername(username);
        if(userEntity==null){
            throw new AuthenticationFailedException("401","User with email not found");
        }
        final String encryptedPassword = passwordCryptographyProvider.encrypt(password, userEntity.getSalt());
        if(encryptedPassword.equals(userEntity.getPassword())){
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthEntity userAuthToken = new UserAuthEntity();
            userAuthToken.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(now);
            userAuthToken.setExpiresAt(expiresAt);
            userAuthToken.setUuid(userEntity.getUuid());

            userDao.createUserAuth(userAuthToken);
//            userDao.updateUser(userEntity);

            return userAuthToken;
        }
        else{
            throw new AuthenticationFailedException("ATH-002", "Password Failed");
        }
    }

    /**
     * The method implements the business logic for signout endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signout(String authorization) throws SignOutRestrictedException {
        if(authorization==null)
            throw new SignOutRestrictedException("400", "Access Token is null");
        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null)
            throw new SignOutRestrictedException("400", "Invalid Access Token");
        if(userAuthEntity.getLogoutAt()!=null)
            throw new SignOutRestrictedException("404","Already Logged Out");

        final ZonedDateTime now = ZonedDateTime.now();
        userAuthEntity.setLogoutAt(now);
        return userDao.updateUserAuth(userAuthEntity);
    }
}
