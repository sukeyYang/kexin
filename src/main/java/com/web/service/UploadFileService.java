package com.web.service;

import com.web.dao.ReportDao;
import com.web.dao.UploadFileDao;
import com.web.entity.ReportCompany;
import com.web.entity.Uploadfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by gaoyang on 16/2/29.
 */
@Service("uploadFileService")
public class UploadFileService {

    @Autowired
    UploadFileDao uploadFileDao;

   public void saveUploadFile(Uploadfile uploadfile){
       uploadFileDao.save(uploadfile);
   }


    public List<Uploadfile> findUploadfileByUUID(String uuid){
        return uploadFileDao.findUploadfileByUUID(uuid);
    }
    public List<Uploadfile> findUploadfileByReportId(String uuid ,String type){
        return uploadFileDao.findUploadfileByReportId(uuid,type);
    }

    public List<Uploadfile> findUploadfileByIDS(List<String> ids){
        return uploadFileDao.findUploadfileByIDS(ids);
    }

    public Uploadfile getUploadfile(String id){
        return (Uploadfile) uploadFileDao.getObjectById(id,Uploadfile.class);
    }
}
