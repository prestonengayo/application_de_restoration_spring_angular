package com.quest.etna;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quest.etna.model.User;
import com.quest.etna.model.UserRole;
import com.quest.etna.repositories.UserRepository;
import com.quest.etna.repositories.AddressRepository;
import com.quest.etna.repositories.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.quest.etna.model.MenuItem;
import com.quest.etna.model.Order;
import com.quest.etna.repositories.MenuItemRepository;
import com.quest.etna.repositories.OrderItemRepository;
import com.quest.etna.repositories.OrderRepository;
import static org.hamcrest.Matchers.is;


@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    
    @Autowired
    private CategoryRepository categoryRepository;



    private ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;
    private String adminToken;

    @BeforeEach
    public void setup() throws Exception {
        // Nettoyer la base de données avant chaque test
        addressRepository.deleteAll();
        userRepository.deleteAll();
        orderRepository.deleteAll();
        menuItemRepository.deleteAll();
        orderItemRepository.deleteAll();
        categoryRepository.deleteAll();
        

        // Créer un utilisateur standard
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(UserRole.ROLE_USER);
        user.setFirstName("UserFirstName");
        user.setLastName("UserLastName");
        userRepository.save(user);

        // Créer un administrateur
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpassword"));
        admin.setRole(UserRole.ROLE_ADMIN);
        admin.setFirstName("AdminFirstName");
        admin.setLastName("AdminLastName");
        userRepository.save(admin);

        // Authentifier l'utilisateur standard et obtenir son token
        String response = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        userToken = "Bearer " + objectMapper.readTree(response).get("accessToken").asText();


        // Authentifier l'administrateur et obtenir son token
        response = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"admin\", \"password\": \"adminpassword\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        adminToken = "Bearer " + objectMapper.readTree(response).get("accessToken").asText();

    }

    @Test
    public void testAuthenticate() throws Exception {
        // Tester que /register répond bien en 201
    	mockMvc.perform(post("/register")
    	        .contentType(MediaType.APPLICATION_JSON)
    	        .content("{\"username\": \"newuser\", \"password\": \"newpassword\", \"firstName\": \"New\", \"lastName\": \"User\"}"))
    	        .andExpect(status().isCreated())
    	        .andDo(result -> System.out.println("Register response: " + result.getResponse().getContentAsString()));


        // Tester que rappeler /register avec les mêmes paramètres retourne 409
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"newuser\", \"password\": \"newpassword\"}"))
                .andExpect(status().isConflict())
                .andDo(result -> System.out.println("Register conflict response: " + result.getResponse().getContentAsString()));

        // Tester que /authenticate retourne un statut 200 ainsi que le token
        String response = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"newuser\", \"password\": \"newpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn().getResponse().getContentAsString();

        String newUserToken = "Bearer " + objectMapper.readTree(response).get("accessToken").asText();
        System.out.println("New User token: " + newUserToken);

        // Tester que /me retourne un statut 200 avec les informations de l'utilisateur
        mockMvc.perform(get("/me")
                .header("Authorization", newUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.firstName", is("New")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andDo(result -> System.out.println("Me response: " + result.getResponse().getContentAsString()));

    }

    @Test
    public void testUser() throws Exception {
        // Sans token Bearer, la route /user retourne bien un statut 401
        mockMvc.perform(get("/user/"))
                .andExpect(status().isUnauthorized())
                .andDo(result -> System.out.println("User route without token response: " + result.getResponse().getContentAsString()));

        // Avec un token Bearer valide, la route /user retourne bien un statut 200
     // Avec un token Bearer valide, la route /user retourne bien un statut 200
        mockMvc.perform(get("/user/")
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'user')].firstName", hasItem("UserFirstName")))
                .andExpect(jsonPath("$[?(@.username == 'user')].lastName", hasItem("UserLastName")))
                .andDo(result -> System.out.println("User route with token response: " + result.getResponse().getContentAsString()));


        // Récupérer les IDs des utilisateurs
        Integer userId = userRepository.findByUsername("user").getId();
        Integer adminId = userRepository.findByUsername("admin").getId();

        // Avec un ROLE_USER, la suppression retourne bien un statut 403
        mockMvc.perform(delete("/user/{id}", adminId)
                .header("Authorization", userToken))
                .andExpect(status().isForbidden())
                .andDo(result -> System.out.println("Delete user with ROLE_USER response: " + result.getResponse().getContentAsString()));

        // Avec un ROLE_ADMIN, la suppression retourne bien un statut 200
        mockMvc.perform(delete("/user/{id}", userId)
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andDo(result -> System.out.println("Delete user with ROLE_ADMIN response: " + result.getResponse().getContentAsString()));
    }

	@Test
	public void testAddress() throws Exception {
	    // Test d'accès non authentifié
	    mockMvc.perform(get("/address/"))
	            .andExpect(status().isUnauthorized())
	            .andDo(result -> System.out.println("Address route without token response: " + result.getResponse().getContentAsString()));
	
	    // Test d'accès authentifié avec un token utilisateur
	    mockMvc.perform(get("/address")
	            .header("Authorization", userToken))
	            .andExpect(status().isOk())
	            .andDo(result -> System.out.println("Address route with user token response: " + result.getResponse().getContentAsString()));
	
	    // Ajouter une adresse avec un token utilisateur
	    String addressJson = "{\"street\": \"123 Main St\", \"city\": \"Anytown\", \"postalCode\": \"12345\", \"country\": \"USA\"}";
	    String response = mockMvc.perform(post("/address")
	            .header("Authorization", userToken)
	            .contentType(MediaType.APPLICATION_JSON)  // Utilisation de MediaType.APPLICATION_JSON
	            .content(addressJson))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.id").exists()) // Vérifie que le champ 'id' est présent dans la réponse
	            .andReturn().getResponse().getContentAsString();
	
	    int addressId = objectMapper.readTree(response).get("id").asInt();
	    System.out.println("Address ID: " + addressId);
	
	    // Ajouter une adresse avec un token administrateur
	    String adminAddressJson = "{\"street\": \"456 Admin St\", \"city\": \"Admintown\", \"postalCode\": \"67890\", \"country\": \"USA\"}";
	    response = mockMvc.perform(post("/address")
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)  // Utilisation de MediaType.APPLICATION_JSON
	            .content(adminAddressJson))
	            .andExpect(status().isCreated())
	            .andReturn().getResponse().getContentAsString();
	
	    int adminAddressId = objectMapper.readTree(response).get("id").asInt();
	    System.out.println("Admin Address ID: " + adminAddressId);
	
	    // Test de suppression d'adresse avec un utilisateur
	    mockMvc.perform(delete("/address/{id}", adminAddressId)
	            .header("Authorization", userToken))
	            .andExpect(status().isForbidden())
	            .andDo(result -> System.out.println("Delete address with user token response: " + result.getResponse().getContentAsString()));
	
	    // Test de suppression d'adresse avec un administrateur
	    mockMvc.perform(delete("/address/{id}", addressId)
	            .header("Authorization", adminToken))
	            .andExpect(status().isOk())
	            .andDo(result -> System.out.println("Delete address with admin token response: " + result.getResponse().getContentAsString()));
	
	    // Test d'accès authentifié avec un token administrateur pour récupérer les adresses
	    mockMvc.perform(get("/address")
	            .header("Authorization", adminToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$").isArray())  // Vérifie que la réponse est un tableau JSON
	            .andDo(result -> System.out.println("Admin GET address response: " + result.getResponse().getContentAsString()));
	}

	
	@Test
	public void testCategoryCRUDOperations() throws Exception {
	    // Création de la catégorie
	    String categoryJson = "{\"name\": \"Snacks\"}";
	    
	    String response = mockMvc.perform(post("/category")
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(categoryJson))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.name").value("Snacks"))
	            .andReturn().getResponse().getContentAsString();

	    Integer categoryId = objectMapper.readTree(response).get("id").asInt();
	    
	    // Récupération de la catégorie
	    mockMvc.perform(get("/category/" + categoryId)
	            .header("Authorization", adminToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.name").value("Snacks"));

	    // Mise à jour de la catégorie
	    String updatedCategoryJson = "{\"name\": \"Updated Snacks\"}";
	    
	    mockMvc.perform(put("/category/" + categoryId)
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(updatedCategoryJson))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.name").value("Updated Snacks"));

	    // Suppression de la catégorie
	    mockMvc.perform(delete("/category/" + categoryId)
	            .header("Authorization", adminToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.message").value("Category deleted successfully"));

	}

	
	
	@Test
	public void testOrderCRUDOperations() throws Exception {
	    // Création de la commande
	    String orderJson = "{\"status\": \"PENDING\"}";

	    String response = mockMvc.perform(post("/order")
	            .header("Authorization", userToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(orderJson))
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.status").value("PENDING"))
	            .andReturn().getResponse().getContentAsString();

	    Integer orderId = objectMapper.readTree(response).get("id").asInt();

	    // Récupération de la commande
	    mockMvc.perform(get("/order/" + orderId)
	            .header("Authorization", userToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.status").value("PENDING"));

	    // Mise à jour de la commande (utilisation de PATCH au lieu de PUT)
	    String updatedOrderJson = "{\"status\": \"COMPLETED\"}";

	    mockMvc.perform(patch("/order/" + orderId)  // Utilisation de PATCH ici
	            .header("Authorization", userToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(updatedOrderJson))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.status").value("COMPLETED"));

	    // Suppression de la commande (garde la même logique ici)
	    mockMvc.perform(delete("/order/" + orderId)
	            .header("Authorization", adminToken))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.message").value("Order deleted successfully"));
	}



	@Test
	public void testMenuItemCRUDOperations() throws Exception {
	    // Créer une catégorie pour le menu item
	    String categoryJson = "{\"name\": \"Burgers\"}";
	    String categoryResponse = mockMvc.perform(post("/category")
	            .header("Authorization", adminToken)
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(categoryJson))
	            .andExpect(status().isCreated())
	            .andReturn().getResponse().getContentAsString();
	    Integer categoryId = objectMapper.readTree(categoryResponse).get("id").asInt();
	
	    // Créer un menu item avec la catégorie associée (multipart/form-data)
	    MockMultipartFile imageFile = new MockMultipartFile("image", "burger.jpg", "image/jpeg", "image content".getBytes());
	
	    // Créer le MenuItem avec les paramètres adéquats
	    mockMvc.perform(multipart("/menu-item")
	            .file(imageFile)
	            .header("Authorization", adminToken)
	            .param("name", "Burger")
	            .param("price", "10.99")
	            .param("description", "Delicious burger")
	            .param("category", "Burgers"))  // Utiliser le nom de la catégorie
	            .andExpect(status().isCreated())
	            .andExpect(jsonPath("$.name").value("Burger"))
	            .andDo(result -> {
	                String menuItemResponse = result.getResponse().getContentAsString();
	                Integer menuItemId = objectMapper.readTree(menuItemResponse).get("id").asInt();
	
	                // Récupérer le menu item
	                mockMvc.perform(get("/menu-item/" + menuItemId)
	                        .header("Authorization", adminToken))
	                        .andExpect(status().isOk())
	                        .andExpect(jsonPath("$.name").value("Burger"));
	
	                // Mettre à jour le menu item (multipart/form-data)
	                MockMultipartFile updatedImageFile = new MockMultipartFile("image", "veggieburger.jpg", "image/jpeg", "updated image content".getBytes());
	
	                mockMvc.perform(MockMvcRequestBuilders.multipart("/menu-item/" + menuItemId)
	                        .file(updatedImageFile)
	                        .header("Authorization", adminToken)
	                        .param("name", "Veggie Burger")
	                        .param("price", "9.99")
	                        .param("description", "Healthy burger")
	                        .param("category", "Burgers")
	                        .with(request -> {
	                            request.setMethod("PUT"); // Changer la méthode en PUT
	                            return request;
	                        }))
	                        .andExpect(status().isOk())
	                        .andExpect(jsonPath("$.name").value("Veggie Burger"));
	
	                // Supprimer le menu item
	                mockMvc.perform(delete("/menu-item/" + menuItemId)
	                        .header("Authorization", adminToken))
	                        .andExpect(status().isOk())
	                        .andExpect(jsonPath("$.message").value("Menu item deleted successfully"));
	            });
	}

	
@Test
public void testOrderItemCRUDOperations() throws Exception {
    // Création d'une catégorie "Fast Food"
    String categoryJson = "{\"name\": \"Fast Food\"}";
    String categoryResponse = mockMvc.perform(post("/category")
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(categoryJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Fast Food"))
            .andReturn().getResponse().getContentAsString();
    
    // Vérification que la catégorie a bien été créée et récupération de son ID
    Integer categoryId = objectMapper.readTree(categoryResponse).get("id").asInt();
    assert categoryId != null : "Category ID should not be null";
    System.out.println("Category ID: " + categoryId);

    // Création d'un menu item associé à cette catégorie (multipart/form-data)
    MockMultipartFile imageFile = new MockMultipartFile("image", "fries.jpg", "image/jpeg", "image content".getBytes());
    String menuItemResponse = mockMvc.perform(multipart("/menu-item")
            .file(imageFile)
            .header("Authorization", adminToken)
            .param("name", "Fries")
            .param("price", "3.50")
            .param("description", "Tasty fries")
            .param("category", "Fast Food"))  // Remplacez category_id par le nom de la catégorie
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Fries"))
            .andReturn().getResponse().getContentAsString();

    
    // Récupération du MenuItem ID
    Integer menuItemId = objectMapper.readTree(menuItemResponse).get("id").asInt();
    assert menuItemId != null : "MenuItem ID should not be null";
    System.out.println("MenuItem ID: " + menuItemId);

    // Création d'une commande associée à l'utilisateur
    String orderJson = "{\"status\": \"PENDING\"}";
    String orderResponse = mockMvc.perform(post("/order")
            .header("Authorization", userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(orderJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn().getResponse().getContentAsString();
    
    // Récupération de l'Order ID
    Integer orderId = objectMapper.readTree(orderResponse).get("id").asInt();
    assert orderId != null : "Order ID should not be null";
    System.out.println("Order ID: " + orderId);

    // Création d'un élément de commande associé à l'order et au menu item
    String orderItemJson = String.format("{\"order\": {\"id\": %d}, \"menuItem\": {\"id\": %d}, \"quantity\": 2, \"price\": 3.50}", orderId, menuItemId);
    String orderItemResponse = mockMvc.perform(post("/order-item")
            .header("Authorization", userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(orderItemJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.price").value(3.50))
            .andReturn().getResponse().getContentAsString();
    
    // Récupération de l'OrderItem ID
    Integer orderItemId = objectMapper.readTree(orderItemResponse).get("id").asInt();
    assert orderItemId != null : "OrderItem ID should not be null";
    System.out.println("OrderItem ID: " + orderItemId);

    // Récupération de l'élément de commande
    mockMvc.perform(get("/order-item/" + orderItemId)
            .header("Authorization", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(2));

    // Mise à jour de l'élément de commande
    String updatedOrderItemJson = String.format("{\"order\": {\"id\": %d}, \"menuItem\": {\"id\": %d}, \"quantity\": 3, \"price\": 3.50}", orderId, menuItemId);
    mockMvc.perform(put("/order-item/" + orderItemId)
            .header("Authorization", userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedOrderItemJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(3));

    // Suppression de l'élément de commande
    mockMvc.perform(delete("/order-item/" + orderItemId)
            .header("Authorization", userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("Order item deleted successfully"));
}



}