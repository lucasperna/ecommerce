package com.lucasperna.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucasperna.dscatalog.dto.ProductDTO;
import com.lucasperna.dscatalog.services.ProductService;
import com.lucasperna.dscatalog.services.exceptions.DatabaseException;
import com.lucasperna.dscatalog.services.exceptions.ResourceNotFoundException;
import com.lucasperna.dscatalog.tests.Factory;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private Long testId;
	private Long failId;
	private Long dependentId;
	private ProductDTO productDto;
	private PageImpl<ProductDTO> page;
	
	@BeforeEach
	void setUp() throws Exception {
		testId = 1L;
		failId = 2L;
		dependentId = 3L;
		
		productDto = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDto));
		
		when(service.findAllPaged(null, "", any())).thenReturn(page);
		
		when(service.findById(testId)).thenReturn(productDto);
		when(service.findById(failId)).thenThrow(ResourceNotFoundException.class);
		
		when(service.update(eq(testId), any())).thenReturn(productDto);
		when(service.update(eq(failId), any())).thenThrow(ResourceNotFoundException.class);
		
		when(service.insert(any())).thenReturn(productDto);
		
		doNothing().when(service).delete(testId);
		doThrow(ResourceNotFoundException.class).when(service).delete(failId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
	}
	
	@Test
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
		
		ResultActions result = mockMvc.perform(delete("/products/{id}", testId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		
		ResultActions result = mockMvc.perform(delete("/products/{id}", failId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void insertShouldReturnProductDTOCreated() throws Exception{
		
		String jsonBody = objectMapper.writeValueAsString(productDto);
		
		ResultActions result = mockMvc.perform(post("/products")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception{
		ResultActions result = mockMvc.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
	}
	
	@Test
	public void findByIdShouldReturnProductWhenIdExist() throws Exception{
		ResultActions result = mockMvc.perform(get("/products/{id}", testId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{
		ResultActions result = mockMvc.perform(get("/products/{id}", failId)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
		
	}
	
	@Test
	public void updateShouldReturnProductDtoWhenIdExist() throws Exception{
		
		String jsonBody = objectMapper.writeValueAsString(productDto);
		
		ResultActions result = mockMvc.perform(put("/products/{id}", testId)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception{
		String jsonBody = objectMapper.writeValueAsString(productDto);
		
		ResultActions result = mockMvc.perform(put("/products/{id}", failId)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(status().isNotFound());
	}
	
}
