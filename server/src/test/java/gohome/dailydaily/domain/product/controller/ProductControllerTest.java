package gohome.dailydaily.domain.product.controller;

import com.google.gson.Gson;
import gohome.dailydaily.domain.member.mapper.SellerMapper;
import gohome.dailydaily.domain.product.controller.dto.GetProductListByDto;
import gohome.dailydaily.domain.product.dto.CategoryGetDto;
import gohome.dailydaily.domain.product.mapper.OptionMapper;
import gohome.dailydaily.domain.product.mapper.ProductMapper;
import gohome.dailydaily.domain.product.service.ProductService;
import gohome.dailydaily.domain.review.mapper.ReviewMapper;
import gohome.dailydaily.global.common.dto.SliceResponseDto;
import gohome.dailydaily.util.security.SecurityTestConfig;
import gohome.dailydaily.util.security.WithMockCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gohome.dailydaily.util.TestConstant.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {ProductController.class, ProductMapper.class, OptionMapper.class, SellerMapper.class, ReviewMapper.class})
@MockBean(JpaMetamodelMappingContext.class)
@Import({SecurityTestConfig.class})
@AutoConfigureRestDocs
@WithMockCustomUser
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void getProduct() throws Exception {

        given(productService.getProduct(PRODUCT.getId()))
                .willReturn(PRODUCT);

        //when
        ResultActions actions = mockMvc.perform(get("/products/details/{product-id}", PRODUCT.getId())
                .accept(MediaType.APPLICATION_JSON));

        MvcResult result = actions.andExpect(status().isOk())
                .andDo(document("products/get",
                        REQUEST_PREPROCESSOR,
                        RESPONSE_PREPROCESSOR,
                        PRODUCT_RESPONSE_FIELDS,
                        PATH_PARAM_PRODUCT_ID
                ))
                .andExpect(jsonPath("$.productId").value(PRODUCT.getId()))
                .andExpect(jsonPath("$.title").value(PRODUCT.getTitle()))
                .andExpect(jsonPath("$.content").value(new Gson().fromJson(PRODUCT.getContent(), List.class)))
                .andExpect(jsonPath("$.price").value(PRODUCT.getPrice()))
                .andExpect(jsonPath("$.img.fileName").value(PRODUCT.getImg().getFileName()))
                .andExpect(jsonPath("$.img.fullPath").value(PRODUCT.getImg().getFullPath()))
                .andExpect(jsonPath("$.score").value(PRODUCT.getScore() / 10F))
                .andExpect(jsonPath("$.seller.sellerId").value(PRODUCT.getSeller().getId()))
                .andExpect(jsonPath("$.seller.memberId").value(PRODUCT.getSeller().getMember().getId()))
                .andReturn();
    }

    @Test
    public void getProductListByCategory() throws Exception {
        SliceResponseDto<CategoryGetDto> products = new SliceResponseDto<>(new SliceImpl<>(
                List.of(new CategoryGetDto(PRODUCT.getId(), PRODUCT.getImg(), PRODUCT.getTitle(),
                                PRODUCT.getPrice(), PRODUCT.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                                PRODUCT.getCategory().getMain()),
                        new CategoryGetDto(PRODUCT2.getId(), PRODUCT2.getImg(), PRODUCT2.getTitle(),
                                PRODUCT2.getPrice(), PRODUCT2.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                                PRODUCT.getCategory().getMain())),PAGEABLE, true));
        given(productService.getProductListByCategory(any(GetProductListByDto.class)))
                .willReturn(products);

        ResultActions actions = mockMvc.perform(
                get("/products")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("main", GET_PRODUCT_LIST_BY_CATEGORY_DTO.getMain())
                        .param("sub", GET_PRODUCT_LIST_BY_CATEGORY_DTO.getSub())
                        .param("page", String.valueOf(PAGEABLE.getPageNumber()))
                        .param("size", String.valueOf(PAGEABLE.getPageSize()))
                        .param("sortType", String.valueOf(PAGEABLE.getSort()).replaceAll(":[^0-9]*", ""))
                        .param("order", String.valueOf(PAGEABLE.getSort()).replaceAll("[^0-9]*: ", ""))

        );

        actions.andExpect(status().isOk())
                .andDo(document("products/category/get",
                        REQUEST_PREPROCESSOR,
                        RESPONSE_PREPROCESSOR,
                        REQUEST_PARAM_CATEGORY,
                        responseFields(
                                FWP_CATEGORY_CONTENT_PRODUCT_ID, FWP_CONTENT_PRODUCT_IMG_NAME, FWP_CONTENT_PRODUCT_IMG_PATH,
                                FWP_CATEGORY_CONTENT_PRODUCT_TITLE, FWP_CONTENT_PRODUCT_PRICE, FWP_CONTENT_PRODUCT_SCORE,
                                FWP_CONTENT_PRODUCT_CATEGORY_MAIN, FWP_CONTENT_PRODUCT_SELLER_NICKNAME,
                                FWP_SLICE_INFO, FWP_SLICE_INFO_PAGE, FWP_SLICE_INFO_SIZE, FWP_SLICE_INFO_HAS_NEXT
                        )));
    }

    @Test
    public void getScoreTop5() throws Exception {
        List<CategoryGetDto> products = new ArrayList<>(List.of(new CategoryGetDto(PRODUCT.getId(), PRODUCT.getImg(), PRODUCT.getTitle(),
                        PRODUCT.getPrice(), PRODUCT.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                        PRODUCT.getCategory().getMain()),
                new CategoryGetDto(PRODUCT2.getId(), PRODUCT2.getImg(), PRODUCT2.getTitle(),
                        PRODUCT2.getPrice(), PRODUCT2.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                        PRODUCT.getCategory().getMain())));

        given(productService.getScoreTop5()).willReturn(products);

        ResultActions actions = mockMvc.perform(
                get("/products/score")
                        .accept(MediaType.APPLICATION_JSON));

        actions.andExpect(status().isOk())
                .andDo(document("products/score/get",
                        REQUEST_PREPROCESSOR,
                        RESPONSE_PREPROCESSOR,
                        responseFields(
                                FWP_SCORE_PRODUCT_ID, FWP_SCORE_PRODUCT_IMG_PATH, FWP_SCORE_PRODUCT_IMG_NAME,
                                FWP_PRODUCTS_SELLER_NICKNAME, FWP_PRODUCTS_CATEGORY_MAIN,
                                FWP_SCORE_PRODUCT_TITLE, FWP_SCORE_PRODUCT_PRICE, FWP_SCORE_PRODUCT_SCORE
                        )));
    }

    @Test
    public void getBrandListLikeTop15() throws Exception {
        List<CategoryGetDto> brand1 = new ArrayList<>(List.of(new CategoryGetDto(PRODUCT.getId(), PRODUCT.getImg(), PRODUCT.getTitle(),
                        PRODUCT.getPrice(), PRODUCT.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                        PRODUCT.getCategory().getMain()),
                new CategoryGetDto(PRODUCT2.getId(), PRODUCT2.getImg(), PRODUCT2.getTitle(),
                        PRODUCT2.getPrice(), PRODUCT2.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                        PRODUCT.getCategory().getMain())));

        List<CategoryGetDto> brand2 = new ArrayList<>(List.of(new CategoryGetDto(PRODUCT.getId(), PRODUCT.getImg(), PRODUCT.getTitle(),
                        PRODUCT.getPrice(), PRODUCT.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                        PRODUCT.getCategory().getMain()),
                new CategoryGetDto(PRODUCT2.getId(), PRODUCT2.getImg(), PRODUCT2.getTitle(),
                        PRODUCT2.getPrice(), PRODUCT2.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                        PRODUCT.getCategory().getMain())));

        HashMap<String ,List<CategoryGetDto>> products = new HashMap<>();
        products.put("nickname",brand1);
        products.put("nickname",brand2);
        given(productService.getBrandListLikeTop15()).willReturn(products);

        ResultActions actions = mockMvc.perform(
                get("/products/brandListLike")
                        .accept(MediaType.APPLICATION_JSON));

        actions.andExpect(status().isOk())
                .andDo(document("products/brandList/get",
                        REQUEST_PREPROCESSOR,
                        RESPONSE_PREPROCESSOR,
                        responseFields(
                                FWP_BRAND_PRODUCT_ID, FWP_BRAND_PRODUCT_IMG_PATH, FWP_BRAND_PRODUCT_IMG_NAME,
                                FWP_BRAND_PRODUCT_TITLE, FWP_BRAND_PRODUCT_PRICE, FWP_BRAND_PRODUCT_SCORE,
                                FWP_BRAND_PRODUCTS_SELLER_NICKNAME, FWP_BRAND_PRODUCTS_CATEGORY_MAIN
                        )));
    }

    @Test
    public void getCategoryCreatedTop5() throws Exception {
        List<CategoryGetDto> category1 = new ArrayList<>(
                List.of(new CategoryGetDto(PRODUCT.getId(), PRODUCT.getImg(), PRODUCT.getTitle(),
                                PRODUCT.getPrice(), PRODUCT.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                                PRODUCT.getCategory().getMain()),
                        new CategoryGetDto(PRODUCT.getId(), PRODUCT.getImg(), PRODUCT.getTitle(),
                                PRODUCT.getPrice(), PRODUCT.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                                PRODUCT.getCategory().getMain())));

        List<CategoryGetDto> category2 = new ArrayList<>(
                List.of(new CategoryGetDto(PRODUCT2.getId(), PRODUCT2.getImg(), PRODUCT2.getTitle(),
                                PRODUCT2.getPrice(), PRODUCT2.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                                PRODUCT.getCategory().getMain()),
                        new CategoryGetDto(PRODUCT2.getId(), PRODUCT2.getImg(), PRODUCT2.getTitle(),
                                PRODUCT2.getPrice(), PRODUCT2.getScore(), PRODUCT.getSeller().getMember().getNickname(),
                                PRODUCT.getCategory().getMain())));

        HashMap<String,List<CategoryGetDto>> products = new HashMap<>();
        products.put("categoryMain",category1);
        products.put("categoryMain",category2);
        given(productService.getCategoryCreatedTop5()).willReturn(products);

        ResultActions actions = mockMvc.perform(
                get("/products/categoryCreated")
                        .accept(MediaType.APPLICATION_JSON));

        actions.andExpect(status().isOk())
                .andDo(document("products/categoryCreated/get",
                        REQUEST_PREPROCESSOR,
                        RESPONSE_PREPROCESSOR,
                        responseFields(
                                FWP_CATEGORY_PRODUCT_ID, FWP_CATEGORY_PRODUCT_IMG_PATH, FWP_CATEGORY_PRODUCT_IMG_NAME,
                                FWP_CATEGORY_PRODUCT_TITLE, FWP_CATEGORY_PRODUCT_PRICE, FWP_CATEGORY_PRODUCT_SCORE,
                                FWP_CATEGORY_PRODUCTS_SELLER_NICKNAME, FWP_CATEGORY_PRODUCTS_CATEGORY_MAIN
                        )));
    }

}
