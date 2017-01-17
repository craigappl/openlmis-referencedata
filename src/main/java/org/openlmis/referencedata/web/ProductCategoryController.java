package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.ProductCategoryMessageKeys;
import org.openlmis.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Controller
public class ProductCategoryController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryController.class);

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  /**
   * Constructor for controller unit testing.
   *
   * @param repository      a ProductCategoryRepository
   */
  public ProductCategoryController(ProductCategoryRepository repository) {
    this.productCategoryRepository = Objects.requireNonNull(repository);
  }

  /**
   * Get all productCategories.
   *
   * @return ProductCategories.
   */
  @RequestMapping(value = "/productCategories", method = RequestMethod.GET)
  public ResponseEntity<?> getAllProductCategories() {
    Iterable<ProductCategory> productCategories = productCategoryRepository.findAll();
    return new ResponseEntity<>(productCategories, HttpStatus.OK);
  }

  /**
   * Create a {@link ProductCategory}.
   *
   * @param productCategory A productCategory bound to the request body
   * @return ResponseEntity containing the created productCategory with id.
   */
  @RequestMapping(value = "/productCategories", method = RequestMethod.POST)
  public ResponseEntity<?> createProductCategory(@RequestBody ProductCategory productCategory) {
    ProductCategory found = productCategoryRepository.findByCode(productCategory
        .getCode());
    if (null != found) {
      found.updateFrom(productCategory);
    } else {
      found = productCategory;
    }

    productCategoryRepository.save(found);
    return new ResponseEntity<>(found, HttpStatus.CREATED);
  }

  /**
   * Updates the given {@link ProductCategory}.  Uses the ID given to base it's update and ignores
   * the code given.
   *
   * @param productCategory   A productCategory bound to the request body
   * @param productCategoryId UUID of productCategory which we want to update
   * @return ResponseEntity containing the updated productCategory
   */
  @RequestMapping(value = "/productCategories/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateProductCategory(@RequestBody ProductCategory productCategory,
                                                 @PathVariable("id") UUID productCategoryId) {
    LOGGER.debug("Updating productCategory with id: " + productCategoryId);

    ProductCategory productCategoryToUpdate =
        productCategoryRepository.findOne(productCategoryId);

    if (null == productCategoryToUpdate) {
      throw new ValidationMessageException(new Message(
          ProductCategoryMessageKeys.ERROR_NOT_FOUND_WITH_ID, productCategoryId));
    }
    productCategoryToUpdate.updateFrom(productCategory);
    productCategoryRepository.save(productCategoryToUpdate);

    LOGGER.debug("Updated productCategory with id: " + productCategoryId);
    return new ResponseEntity<>(productCategoryToUpdate, HttpStatus.OK);
  }

  /**
   * Get chosen productCategory.
   *
   * @param productCategoryId UUID of productCategory which we want to get
   * @return ProductCategory.
   */
  @RequestMapping(value = "/productCategories/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getProductCategory(@PathVariable("id") UUID productCategoryId) {
    ProductCategory productCategory = productCategoryRepository.findOne(productCategoryId);
    if (productCategory == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(productCategory, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting productCategory.
   *
   * @param productCategoryId UUID of productCategory which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/productCategories/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteProductCategory(@PathVariable("id") UUID productCategoryId) {

    ProductCategory productCategory = productCategoryRepository.findOne(productCategoryId);
    if (productCategory == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      try {
        productCategoryRepository.delete(productCategory);
      } catch (DataIntegrityViolationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "An error occurred while deleting product category with ID: " + productCategoryId,
            ex.getMessage());
        LOGGER.error(errorResponse.getMessage(), ex);
        return new ResponseEntity(HttpStatus.CONFLICT);
      }
      return new ResponseEntity<ProductCategory>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Finds ProductCategories matching all of provided parameters.
   *
   * @param codeParam code of productCategory.
   * @return ResponseEntity with list of all Product Categories matching provided parameters
   */
  @RequestMapping(value = "/productCategories/search", method = RequestMethod.GET)
  public ResponseEntity<?> searchProductCategories(
      @RequestParam(value = "code", required = false) String codeParam) {

    if (codeParam != null) {
      ProductCategory productCategory = productCategoryRepository
          .findByCode(Code.code(codeParam));
      if (null == productCategory) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(Arrays.asList(productCategory));
    } else {
      Iterable<ProductCategory> productCategories = productCategoryRepository.findAll();
      return ResponseEntity.ok(productCategories);
    }
  }
}
