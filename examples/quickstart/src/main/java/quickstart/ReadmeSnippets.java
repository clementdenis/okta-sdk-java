/*
 * Copyright 2018-Present Okta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quickstart;

import com.fasterxml.jackson.core.type.TypeReference;
import com.okta.commons.http.MediaType;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.cache.Caches;
import com.okta.sdk.client.AuthenticationScheme;
import com.okta.sdk.client.AuthorizationMode;
import com.okta.sdk.client.Clients;
import com.okta.sdk.helper.PaginationUtil;
import com.okta.sdk.resource.api.GroupApi;
import com.okta.sdk.resource.api.UserApi;
import com.okta.sdk.resource.group.GroupBuilder;
import com.okta.sdk.resource.user.UserBuilder;

import com.okta.sdk.resource.client.ApiClient;
import com.okta.sdk.resource.client.ApiException;
import com.okta.sdk.resource.client.Pair;
import com.okta.sdk.resource.api.*;
import com.okta.sdk.resource.model.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.okta.sdk.cache.Caches.forResource;

/**
 * Example snippets used for this projects README.md.
 * <p>
 * Manually run {@code mvn okta-code-snippet:snip} after changing this file to update the README.md.
 */
@SuppressWarnings({"unused"})
public class ReadmeSnippets {

    private static final Logger log = LoggerFactory.getLogger(ReadmeSnippets.class);

    private final ApiClient client = Clients.builder().build();
    private static final User user = null;

    private void createClient() {
        ApiClient client = Clients.builder()
            .setOrgUrl("https://{yourOktaDomain}")  // e.g. https://dev-123456.okta.com
            .setClientCredentials(new TokenClientCredentials("{apiToken}"))
            .build();
    }

    private void createOAuth2Client() {
        ApiClient client = Clients.builder()
            .setOrgUrl("https://{yourOktaDomain}")  // e.g. https://dev-123456.okta.com
            .setAuthorizationMode(AuthorizationMode.PRIVATE_KEY)
            .setClientId("{clientId}")
            .setKid("{kid}") // optional
            .setScopes(new HashSet<>(Arrays.asList("okta.users.manage", "okta.apps.manage", "okta.groups.manage")))
            .setPrivateKey("/path/to/yourPrivateKey.pem")
            // (or) .setPrivateKey("full PEM payload")
            // (or) .setPrivateKey(Paths.get("/path/to/yourPrivateKey.pem"))
            // (or) .setPrivateKey(inputStream)
            // (or) .setPrivateKey(privateKey)
            // (or) .setOAuth2AccessToken("access token string") // if set, private key (if supplied) will be ignored
            .build();
    }

    private void getUser() throws ApiException {
        UserApi userApi = new UserApi(client);

        userApi.getUser("userId", "application/json", "true");
    }

    private void listAllUsers() throws ApiException {
        UserApi userApi = new UserApi(client);
        List<User> users = userApi.listUsers("application/json", null, null, 5, null, null, null, null);

        // stream
        users.stream()
            .forEach(user -> {
              // do something
            });
    }

    private void userSearch() throws ApiException {
        UserApi userApi = new UserApi(client);
        // search by email
        List<User> users = userApi.listUsers("application/json", null, null, 5, null, "profile.email eq \"jcoder@example.com\"", null, null);

        // filter parameter
        userApi.listUsers("application/json",null, null, null, "status eq \"ACTIVE\"",null, null, null);
    }

    private void createUser() throws ApiException {
        UserApi userApi = new UserApi(client);

        User user = UserBuilder.instance()
            .setEmail("joe.coder@example.com")
            .setFirstName("Joe")
            .setLastName("Code")
            .buildAndCreate(userApi);
    }

    private void createUserWithGroups() throws ApiException {
        UserApi userApi = new UserApi(client);

        User user = UserBuilder.instance()
            .setEmail("joe.coder@example.com")
            .setFirstName("Joe")
            .setLastName("Code")
            .setGroups(Arrays.asList("groupId-1", "groupId-2"))
            .buildAndCreate(userApi);
    }

    private void updateUser() throws ApiException {
        UserApi userApi = new UserApi(client);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        UserProfile userProfile = new UserProfile();
        userProfile.setNickName("Batman");
        updateUserRequest.setProfile(userProfile);

        userApi.updateUser(user.getId(), updateUserRequest, true);
    }

    private void updateUserWithCustomAttributes() throws ApiException {
        UserApi userApi = new UserApi(client);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        UserProfile userProfile = new UserProfile();

        userProfile.getAdditionalProperties().put("foo", "bar");

        updateUserRequest.setProfile(userProfile);

        userApi.updateUser(user.getId(), updateUserRequest, true);
    }

    private void deleteUser() throws ApiException {
        UserApi userApi = new UserApi(client);

        // deactivate first
        userApi.deleteUser(user.getId(), false, null);

        // then delete
        // see https://developer.okta.com/docs/api/openapi/okta-management/management/tag/User/#tag/User/operation/deleteUser
        userApi.deleteUser(user.getId(), false, null);
    }

    private void listGroups() throws ApiException {
        GroupApi groupApi = new GroupApi(client);

        List<Group> groups = groupApi.listGroups(null, null, null, 10, null, null, null, null);
    }

    private void createGroup() throws ApiException {
        GroupApi groupApi = new GroupApi(client);

        Group group = GroupBuilder.instance()
            .setName("a-group-name")
            .setDescription("Example Group")
            .buildAndCreate(groupApi);
    }

    private void assignUserToGroup() throws ApiException {
        // create user
        UserApi userApi = new UserApi(client);

        User user = UserBuilder.instance()
            .setEmail("joe.coder@example.com")
            .setFirstName("Joe")
            .setLastName("Code")
            .buildAndCreate(userApi);

        // create group
        GroupApi groupApi = new GroupApi(client);

        Group group = GroupBuilder.instance()
            .setName("a-group-name")
            .setDescription("Example Group")
            .buildAndCreate(groupApi);

        // assign user to group
        groupApi.assignUserToGroup(group.getId(), user.getId());
    }

    private void listUserFactors() throws ApiException {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        List<UserFactor> userFactors = userFactorApi.listFactors("userId");
    }

    private void getUserFactor() throws ApiException {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        UserFactor userFactor = userFactorApi.getFactor("userId", "factorId");
    }

    private void enrollUserInFactor() throws ApiException {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        UserFactorSMSProfile UserFactorSMSProfile = new UserFactorSMSProfile();
        UserFactorSMSProfile.setPhoneNumber("555 867 5309");

        UserFactorSMS UserFactorSMS = new UserFactorSMS();
        UserFactorSMS.setProvider(UserFactorProvider.OKTA);
        UserFactorSMS.setFactorType(UserFactorType.SMS);
        UserFactorSMS.setProfile(UserFactorSMSProfile);

        userFactorApi.enrollFactor("userId", UserFactorSMS, true, "templateId", 30, true, null);
    }

    private void activateFactor() throws ApiException {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        UserFactorCall userFactor = (UserFactorCall) userFactorApi.getFactor("userId", "factorId");
        UserFactorActivateRequest userFactorActivateRequest = new UserFactorActivateRequest();
        userFactorActivateRequest.setPassCode("123456");

        userFactorApi.activateFactor("userId", "factorId", userFactorActivateRequest);
    }

    private void verifyFactor() throws ApiException {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        UserFactor userFactor = userFactorApi.getFactor( "userId", "factorId");
        UserFactorVerifyRequest userFactorVerifyRequest = new UserFactorVerifyRequest();
        userFactorVerifyRequest.setPassCode("123456");

        UserFactorVerifyResponse verifyUserFactorResponse =
            userFactorApi.verifyFactor("userId", "factorId", "templateId", 10, "xForwardedFor", "userAgent", "acceptLanguage", userFactorVerifyRequest);
    }

    private void listApplications() throws ApiException {
        ApplicationApi applicationApi = new ApplicationApi(client);

        List<Application> applications = applicationApi.listApplications(null, null, true, null, null, null, true);
    }

    private void getApplication() throws ApiException {
        ApplicationApi applicationApi = new ApplicationApi(client);

        BookmarkApplication bookmarkApp = (BookmarkApplication) applicationApi.getApplication("bookmark-app-id", null);
    }

    private void createSwaApplication() throws ApiException {
        ApplicationApi applicationApi = new ApplicationApi(client);

        SwaApplicationSettingsApplication swaApplicationSettingsApplication = new SwaApplicationSettingsApplication();
        swaApplicationSettingsApplication.buttonField("btn-login")
            .passwordField("txtbox-password")
            .usernameField("txtbox-username")
            .url("https://example.com/login.html");
        SwaApplicationSettings swaApplicationSettings = new SwaApplicationSettings();
        swaApplicationSettings.app(swaApplicationSettingsApplication);
        BrowserPluginApplication browserPluginApplication = new BrowserPluginApplication();
        browserPluginApplication.name(BrowserPluginApplication.NameEnum.TEMPLATE_SWA);
        browserPluginApplication.label("Sample Plugin App");
        browserPluginApplication.settings(swaApplicationSettings);

        // create BrowserPluginApplication app type
        BrowserPluginApplication createdApp =
                (BrowserPluginApplication) applicationApi.createApplication(browserPluginApplication, true, null);
    }

    private void listPolicies() throws ApiException {
        PolicyApi policyApi = new PolicyApi(client);

        List<Policy> policies = policyApi.listPolicies(PolicyType.PASSWORD.name(), LifecycleStatus.ACTIVE.name(), null, null, null, null, null, null);
    }

    private void getPolicy() throws ApiException {
        PolicyApi policyApi = new PolicyApi(client);

        Policy policy =
            policyApi.getPolicy("policy-id", null);
    }

    private void listSysLogs() throws ApiException {
        SystemLogApi systemLogApi = new SystemLogApi(client);

        // use a filter (start date, end date, filter, or query, sort order) all options are nullable
        List<LogEvent> logEvents =
            systemLogApi.listLogEvents(null, null, null, "interestingURI.com", null, 100, "ASCENDING");
    }

    private void callAnotherEndpoint() throws ApiException {

        ApiClient apiClient = buildApiClient("orgBaseUrl", "apiKey");

        // Create a User
        String email = "joe.coder+" + UUID.randomUUID() + "@example.com";

        UserProfile userProfile = new com.okta.sdk.resource.model.UserProfile()
            .firstName("Joe")
            .lastName("Coder")
            .email(email)
            .mobilePhone("1234567890")
            .login(email);

        com.okta.sdk.resource.model.CreateUserRequest createUserRequest = new com.okta.sdk.resource.model.CreateUserRequest();
        createUserRequest.setProfile(userProfile);

        List<com.okta.sdk.resource.client.Pair> queryParams = new ArrayList<com.okta.sdk.resource.client.Pair>();
        queryParams.addAll(client.parameterToPair("activate", "true"));
        queryParams.addAll(client.parameterToPair("provider", null));
        queryParams.addAll(client.parameterToPair("nextLogin", null));

        List<com.okta.sdk.resource.client.Pair> collectionQueryParams = new ArrayList<com.okta.sdk.resource.client.Pair>();

        Map<String, String> headerParams = new HashMap<String, String>();
        Map<String, String> cookieParams = new HashMap<String, String>();
        Map<String, Object> formParams = new HashMap<String, Object>();

        TypeReference<com.okta.sdk.resource.model.User> returnType = new TypeReference<com.okta.sdk.resource.model.User>() {
        };

        com.okta.sdk.resource.model.User user = client.invokeAPI(
            "/api/v1/users",
            "POST",
            queryParams,
            collectionQueryParams,
            new StringJoiner("&").toString(),
            createUserRequest,
            headerParams,
            cookieParams,
            formParams,
            "application/json",
            "application/json",
            new String[] { "apiToken", "oauth2" },
            returnType);

    }

    private void paginate() throws ApiException {
        UserApi userApi = new UserApi(client);

        List<User> users = new ArrayList<>();
        String after = null;

        do {
            users.addAll(userApi.listUsers("application/json",null, after, 200, null, null, null, null));
            after = PaginationUtil.getAfter(userApi.getApiClient());
        } while (StringUtils.isNotBlank(after));
    }

    private void complexCaching() {
        Caches.newCacheManager()
            .withDefaultTimeToLive(300, TimeUnit.SECONDS) // default
            .withDefaultTimeToIdle(300, TimeUnit.SECONDS) //general default
            .withCache(forResource(User.class) //User-specific cache settings
                .withTimeToLive(1, TimeUnit.HOURS)
                .withTimeToIdle(30, TimeUnit.MINUTES))
            .withCache(forResource(Group.class) //Group-specific cache settings
                .withTimeToLive(1, TimeUnit.HOURS))
            //... etc ...
            .build(); //build the CacheManager
    }

    private void disableCaching() {
        ApiClient client = Clients.builder()
            .setCacheManager(Caches.newDisabledCacheManager())
            .build();
    }

    private static ApiClient buildApiClient(String orgBaseUrl, String apiKey) {

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(orgBaseUrl);
        apiClient.setApiKey(apiKey);
        apiClient.setApiKeyPrefix(AuthenticationScheme.SSWS.name());
        return apiClient;
    }
}
