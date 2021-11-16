package com.lucasperna.dscatalog.services;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucasperna.dscatalog.dto.ProductDTO;
import com.lucasperna.dscatalog.entities.Category;
import com.lucasperna.dscatalog.entities.Product;
import com.lucasperna.dscatalog.repositories.CategoryRepository;
import com.lucasperna.dscatalog.repositories.ProductRepository;
import com.lucasperna.dscatalog.services.exceptions.DatabaseException;
import com.lucasperna.dscatalog.services.exceptions.ResourceNotFoundException;
import com.lucasperna.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	
	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long testId;
	private long failId;
	private long dependentId;
	private PageImpl<Product> page;
	private Product product;
	private ProductDTO productDto;
	private Category category;
	
	@BeforeEach
	void setUp() throws Exception {
		testId = 1L;
		failId = 2L;
		dependentId = 3L;
		product = Factory.createProduct();
		productDto = Factory.createProductDTO();
		category = Factory.createCategory();
		page = new PageImpl<>(List.of(product));
		
		Mockito.when(repository.findAll((Pageable)any())).thenReturn(page);
		
		Mockito.when(repository.save(any())).thenReturn(product);
		
		Mockito.when(repository.findById(testId)).thenReturn(Optional.of(product));
		Mockito.when(repository.findById(failId)).thenReturn(Optional.empty());
		
		Mockito.when(repository.find(any(), any(), any())).thenReturn(page);
		
		Mockito.when(repository.getOne(testId)).thenReturn(product);
		Mockito.when(repository.getOne(failId)).thenThrow(EntityNotFoundException.class);
		
		Mockito.when(categoryRepository.getOne(testId)).thenReturn(category);
		Mockito.when(categoryRepository.getOne(failId)).thenThrow(EntityNotFoundException.class);
		
		Mockito.doNothing().when(repository).deleteById(testId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(failId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptioWhenDoesNotExistsId() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(failId, productDto);
		});
	}
	
	@Test
	public void updateShouldReturnProductDtoUpdatedWhenExistsId() {
		
		ProductDTO result = service.update(testId, productDto);
		
		Assertions.assertNotNull(result);
	}
	
	@Test
	public void findByIdShouldReturnProductDtoWhenExistsId() {
		
		ProductDTO result = service.findById(testId);
		
		Assertions.assertNotNull(result);
		
		Mockito.verify(repository).findById(testId);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenDoesNotExistsId() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(failId);
		});
		
		Mockito.verify(repository).findById(failId);
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(testId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(testId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(failId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(failId);
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenIdDependent() {
		
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(0L, "", pageable);
		
		Assertions.assertNotNull(result);
	}
}
