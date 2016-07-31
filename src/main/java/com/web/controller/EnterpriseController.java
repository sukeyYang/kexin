package com.web.controller;

import java.util.List;
import java.util.Map;

import com.utils.DateUtil;
import com.utils.StringUtil;
import com.web.entity.RecordInfo;
import com.web.model.*;
import com.web.service.EnterpriseService;
import com.web.service.RecordInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by sukey on 2016/7/8.
 */
@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private RecordInfoService recordInfoService;

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public List<EnterpriseModel> search(@RequestParam(required = false) String keywords, @RequestParam(required = false) String page, @RequestParam(required = false) String pageSize) {

        List<EnterpriseModel> list = enterpriseService.findEnterpriseInfoByKeyWords(keywords, page, pageSize);
        return list;
    }

    @RequestMapping(value = "detail", method = RequestMethod.GET)
    public EnterpriseDetailModel detail(@RequestParam String id, @RequestParam String record_no, String userid) {
        List<RecordInfo> recordInfoList = enterpriseService.findEnterpriseInfoByID(id);
        EnterpriseDetailModel model = new EnterpriseDetailModel();
        if (recordInfoList.size() > 0) {
            RecordInfo recordInfo = recordInfoList.get(0);
            model.setReg_address(recordInfo.getReg_address());
            model.setCom_name(recordInfo.getCom_name());
            model.setRole_type(recordInfo.getRole_type());
            String user_type = StringUtil.safeToString(recordInfo.getUser_type(), "");
            if ("10".equals(user_type)) {
                model.setRole_type("生产型企业或机构");
            } else if ("20".equals(user_type)) {
                model.setRole_type("经营型企业或机构");
            } else if ("30".equals(user_type)) {
                model.setRole_type("服务型企业或机构");
            } else if ("50".equals(user_type)) {
                model.setRole_type("企业或机构消费者");
            } else if ("60".equals(user_type)) {
                model.setRole_type("个人消费者");
            }
            model.setAddress(recordInfo.getAddress());
            model.setAnnual_check(recordInfo.getAnnual_check());
            model.setFound_date(DateUtil.FormatUIDate(recordInfo.getFound_date()));
            model.setIs_manu_import(recordInfo.getIs_manu_import());
            model.setLaw_person(recordInfo.getLaw_person());
            model.setLp_phone(recordInfo.getLp_phone());
            model.setReg_branch(recordInfo.getReg_branch());
            model.setReg_capital(recordInfo.getReg_capital());
            model.setReg_on(recordInfo.getReg_no());
            String collected = recordInfoService.findCollect(recordInfo.getUsername(), 1, userid);
            if ("0".equals(collected)) {
                model.setCollected(false);
            } else {
                model.setCollected(true);
            }
            model.setValid_period(DateUtil.FormatUIDate(recordInfo.getValid_period()));
        }
        List<CertificateModel> certificateList = enterpriseService.findCertificateByRecordNo(record_no);
        model.setCertificateModels(certificateList);
        //记录日志
        recordInfoService.setlog(userid, 1, id);
        return model;
    }

    @RequestMapping(value = "collected", method = RequestMethod.GET)
    public List<EnterpriseModel> collected(@RequestParam String userid, @RequestParam(required = false) String keywords, @RequestParam(required = false) String page, @RequestParam(required = false) String pageSize) {

        List<EnterpriseModel> list = enterpriseService.findCollectdEnterpriseInfo(userid, keywords, page, pageSize);
        return list;
    }


}
