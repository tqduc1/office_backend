package com.cmcglobal.backend.service.impl;

import com.cmcglobal.backend.dto.poa.UserLogin;
import com.cmcglobal.backend.dto.poa.UserManager;
import com.cmcglobal.backend.dto.request.auth.LoginRequest;
import com.cmcglobal.backend.dto.request.auth.PayloadDTO;
import com.cmcglobal.backend.dto.response.auth.LogoutResponse;
import com.cmcglobal.backend.entity.UserFlattened;
import com.cmcglobal.backend.repository.UserFlattenedRepository;
import com.cmcglobal.backend.service.PoaService;
import com.cmcglobal.backend.utility.CacheManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PoaServiceImpl implements PoaService {
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserFlattenedRepository userFlattenedRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Map<String, String> getManagersFromPOA() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer eyJTeXN0ZW0iOiJPRkZJQ0UiLCJLZXkiOiI5UzN3V3ZTKzBneWJqdUVCUGMyNnp3PT06UHVlbE1BM0lMOWJZZUJNR3Z4eG1aQT09In0=");
            return execution.execute(request, body);
        });
        ResponseEntity<UserManager> response = restTemplate.getForEntity("https://auth-api.cmcglobal.com.vn/api/group/GetGroupManagers", UserManager.class);

        if (response.getBody().getItem() == null) {
            return null;
        }
        Map<String, String> usernameMap = new HashMap<>();
        for (UserManager.Item item : response.getBody().getItem()) {
            List<UserManager.User> managers = item.getManagers();
            for (UserManager.User manager : managers) {
                if (!usernameMap.containsKey(manager.getUsername())) {
                    usernameMap.put(manager.getUsername(), manager.getUsername());
                }
            }
        }
        return usernameMap;
    }

    @Override
    public List<String> getManagersUsername() {
        return userFlattenedRepository.findAllLeaderUsername();
    }

    @Override
    public void syncUserData() {
        final String uri = "https://gateway.cmcglobal.com.vn/poa/api/user/GetAllUserpoaOffice";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<UserFlattened[]> response = restTemplate.getForEntity(uri, UserFlattened[].class);
        Map<String, String> managersUsernameMap = this.getManagersFromPOA();
        List<UserFlattened> changeUsers = new ArrayList<>();
        for (UserFlattened userPoa : response.getBody()) {
            if (managersUsernameMap.containsKey(userPoa.getUserName())){
                userPoa.setManager(true);
            }
            UserFlattened oldUser = userFlattenedRepository.findByUserName(userPoa.getUserName());
            if (!userPoa.equals(oldUser) || ObjectUtils.isEmpty(oldUser)) {
                changeUsers.add(userPoa);
            }
        }
        log.info("number of users change: {}", changeUsers.size());
        log.info("changed accounts: {}", changeUsers);
        userFlattenedRepository.saveAll(changeUsers);
    }

    @Override
    public List<String> getUsernameListByGroupName(String department) {
        List<UserFlattened> user;
        if (userFlattenedRepository.existsByParentDepartmentName(department)) {
            user = userFlattenedRepository.findAllByParentDepartmentName(department);
        } else {
            user = userFlattenedRepository.findAllByDepartmentName(department);
        }
        return user.stream().map(UserFlattened::getUserName).collect(Collectors.toList());
    }

    @Override
    public UserFlattened getUserInfoByUsername(String username) {
        return userFlattenedRepository.findByUserName(username);
    }

    @Override
    public List<UserFlattened> getListUserInfoByUsername(String username) {
        return userFlattenedRepository.findAllByUserNameLike(username);
    }

    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {

        // Real accounts for live server

//        RestTemplate restTemplate = new RestTemplate();
//        String uri = "https://auth-api.cmcglobal.com.vn/sso/login";
//        PayloadDTO payloadDTO = new PayloadDTO();
//        PayloadDTO.loginDTO loginDTO = new PayloadDTO.loginDTO();
//        loginDTO.setUsername(loginRequest.getUsername());
//        loginDTO.setPassword(loginRequest.getPassword());
//        payloadDTO.setPayload(loginDTO);
//        UserLogin response = restTemplate.postForObject(uri, payloadDTO, UserLogin.class);


        // Fake accounts for testing
        Query query = new Query();
        query.addCriteria(Criteria.where("user.userName").is(loginRequest.getUsername()));
        UserLogin response = mongoTemplate.findOne(query, UserLogin.class, "fake");
        try {
            if (response.getStatus()) {
                cacheManager.set(response.getToken(), response, 3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> logout() {
        cacheManager.del((String) SecurityContextHolder.getContext().getAuthentication().getCredentials());
        return new ResponseEntity<>(new LogoutResponse(true, "login success"), HttpStatus.OK);
    }
}