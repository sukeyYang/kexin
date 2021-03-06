package com.web.controller;

import com.utils.ConvertUtil;
import com.web.entity.*;
import com.web.entity.ProductAttr;
import com.web.model.*;
import com.web.service.RecordInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

//import javax.validation.Valid;

/**
 * Created by gaoyang on 16/2/28.
 */
@RestController
public class ProductController {

    @Autowired
    RecordInfoService recordInfoService;

    private static String IMGURL="http://www.ecdata.org.cn/srv/viewDownloadAction.action?fileName=publishedFile/";

    @RequestMapping(value = "/search-product", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<RepProductModel> listproduct(@RequestParam(required=false ) String productname,@RequestParam(required=false ) String page,@RequestParam(required=false ) String pageSize){
        List<RepProductModel> result=new ArrayList<>();
        Map p=new HashMap();
        p.put("productname",ConvertUtil.safeToString(productname,""));
        p.put("page",page);
        p.put("pageSize",pageSize);
        List<Map> list=recordInfoService.findProduct(p);
        for (Map map:list) {
            RepProductModel product=new RepProductModel();
            String productn=ConvertUtil.safeToString(map.get("PRODUCT_NAME"),"");
            if(!"".equals(productn)){
                product.setProductname(productn);
            }else{
                product.setProductname(ConvertUtil.safeToString(map.get("GENERIC_NAME"),""));
            }
            product.setCompanyname(ConvertUtil.safeToString(map.get("MANUFACTURER"),""));
            product.setId(ConvertUtil.safeToString(map.get("ID"),""));
            product.setEvaluation(ConvertUtil.safeToString(map.get("POINT"),""));
            String picurl= ConvertUtil.safeToString(map.get("MANUAL_FILE"),"");
            if("".equals(picurl)){
                picurl=ConvertUtil.safeToString(map.get("PICURL"),"");
            }
            if(!"".equals(picurl)){
                String ps[]=picurl.split(";");
                product.setPicurl(IMGURL+ps[0]);
            }
            result.add(product);
        }
        return result;
    }


    @RequestMapping(value = "/search-product/{id}/{userid}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public RepProductDetailModel getproduct(@PathVariable String id,@PathVariable String userid) throws NoSuchFieldException {
        RepProductDetailModel productDetailModel=new RepProductDetailModel();
        DraftPermit product = recordInfoService.getProduct(id);
        if(null!=product){
            String productname=ConvertUtil.safeToString(product.getProduct_name(),"");
            if(!"".equals(productname)){
                productDetailModel.setProductname(product.getProduct_name());
            }else{
                productDetailModel.setProductname(product.getGeneric_name());
            }
            productDetailModel.setBrands(ConvertUtil.safeToString(product.getBrand(),""));
            productDetailModel.setCompanyname(ConvertUtil.safeToString(product.getManufacturer(),""));
            productDetailModel.setCampanyaddress(ConvertUtil.safeToString(product.getManu_address(),""));
            if(!"".equals(ConvertUtil.safeToString(product.getProduction_license_file(),""))){
                productDetailModel.setLicense(IMGURL+product.getProduction_license_file());
                productDetailModel.setLicense(productDetailModel.getLicense().replaceAll(";",""));
            }
            String picurl= ConvertUtil.safeToString(product.getManual_file(),"");
            if("".equals(picurl)){
                picurl=ConvertUtil.safeToString(product.getPicurl(),"");
            }
            List<String> list2=new ArrayList<>();
            if(!"".equals(picurl)){
                String ps[]=picurl.split(";");
                for (String s:ps ) {
                    list2.add(IMGURL+s);
                }
            }
            productDetailModel.setProductimg(list2);

            List<Map> webSitelist = recordInfoService.findWebSite(id);
            List<RepWebSite> website=new ArrayList<>();
            for (Map map: webSitelist) {
                RepWebSite ws=new RepWebSite();
                ws.setName(ConvertUtil.safeToString(map.get("WEBSITE_NAME"),""));
                ws.setUrl(ConvertUtil.safeToString(map.get("WEBSITE_ADDRESS"),""));
                website.add(ws);
            }
            productDetailModel.setAuthorize(webSitelist.size());
            productDetailModel.setWebSiteList(website);
            productDetailModel.setEvaluation(recordInfoService.getProductCommentpoint(id));

            //补充
            List<Attr> attrlist=new ArrayList<>();
            List<ProductAttr> productAttrList=recordInfoService.findProductAttr(product.getTemplate_id());
            for (ProductAttr pro:productAttrList) {
                Field field=DraftPermit.class.getDeclaredField(pro.getDraft_field_name().toLowerCase());
                field.setAccessible(true);
                Attr attr=new Attr();
                attr.setDisplay_name(pro.getDisplay_name());
                attr.setDisplay_position(pro.getDisplay_position());
                attr.setDraft_field_name(pro.getDraft_field_name());
                attr.setExtra_info(pro.getExtra_info());
                attr.setInput_type(pro.getInput_type());
                attr.setValue(ConvertUtil.safeToString(ReflectionUtils.getField(field,product),""));
                attrlist.add(attr);
            }
            productDetailModel.setAttrList(attrlist);

            //判断是否收藏 0 无收藏 1 收藏了
            if(!"0".equals(userid)){
                productDetailModel.setIscollect(recordInfoService.findCollect(id,2,userid));

                //判断是否评论 0 无评论 1 有评论
                productDetailModel.setIscomment(recordInfoService.findComment(id,userid));

            }


            productDetailModel.setPasstime(product.getPass_time());

            //记录日志
            recordInfoService.setlog(userid,2,id);
        }
        return productDetailModel;
    }

    @RequestMapping(value="/comment-product", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public  ProductComment addComment(ReqCommentModel commentModel) {
        ProductComment productComment=new ProductComment();
        productComment.setCreate_time(new Date());
        productComment.setUpdate_time(new Date());
        productComment.setDirections(commentModel.getDirections());
        productComment.setUserid(commentModel.getUserid());
        productComment.setProductid(commentModel.getProductid());
        productComment.setPoint(commentModel.getPoint());
        productComment.setIsfalse(commentModel.getIsfalse());
        recordInfoService.saveProductComment(productComment);

        //预警检测
        recordInfoService.runproductreminde(productComment.getDirections(),productComment.getProductid(),productComment.getPoint());
        return productComment;
    }


    @RequestMapping(value = "/search-productcomment", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<ProductCommentModel> listproduct(@RequestParam(required=false ) String productid){
        List<ProductCommentModel> productCommentModelList=new ArrayList<>();
        List<Map> list=recordInfoService.findProductComment2(productid);
        for (Map map:list) {
            ProductCommentModel pc=new ProductCommentModel();
            pc.setIsfalse(ConvertUtil.safeToInteger(map.get("ISFALSE"),0));
            pc.setDirections(ConvertUtil.safeToString(map.get("DIRECTIONS"),""));
            pc.setPoint(ConvertUtil.safeToInteger(map.get("POINT"),0));
            pc.setCreate_time(ConvertUtil.safeToString(map.get("CREATE_TIME"),""));
            pc.setUsername(ConvertUtil.safeToString(map.get("NAME"),""));
            pc.setUserid(ConvertUtil.safeToString(map.get("USERID"),""));
            productCommentModelList.add(pc);
        }
        return productCommentModelList;
    }


}
