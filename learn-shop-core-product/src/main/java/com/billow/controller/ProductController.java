package com.billow.controller;

import com.billow.common.resData.BaseResponse;
import com.billow.common.enums.ResCodeEnum;
import com.billow.common.whiteList.AdminSystemRemote;
import com.billow.pojo.vo.sys.WhiteListVo;
import com.billow.service.TestService;
import com.billow.tools.utlis.SpringContextUtil;
import com.billow.pojo.vo.TestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 测试使用
 *
 * @author liuyongtao
 * @create 2018-05-11 11:03
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TestService testService;

    @Autowired
    private AdminSystemRemote adminSystemRemote;

    @GetMapping("/setStringValue")
    public BaseResponse<String> setStringValue() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        BaseResponse<String> response = new BaseResponse<>(ResCodeEnum.OK);
        opsForValue.set("name", "tom", 20, TimeUnit.SECONDS);
        try {
            ProductController productController = SpringContextUtil.getBean("productController");
            response.setResData(productController.getClass().getName());
        } catch (Exception e) {
            response.setResCode(ResCodeEnum.FAIL);
            e.printStackTrace();
        }
        return response;
    }

    @GetMapping("/getStringValue")
    public BaseResponse<String> getStringValue() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        BaseResponse<String> response = new BaseResponse<>();
        response.setResData(opsForValue.get("name"));
        return response;
    }

    @PostMapping("/saveTest")
    public BaseResponse<TestVo> saveTest() {
        BaseResponse<TestVo> response = new BaseResponse<>();
        try {
            TestVo testVo = new TestVo();
            testVo = testService.saveTest(testVo);
            response.setResData(testVo);
        } catch (Exception e) {
            e.printStackTrace();
            response.setResCode(ResCodeEnum.FAIL);
        }
        return response;
    }

    @GetMapping("/findTestById/{id}")
    public BaseResponse<TestVo> findTestById(@PathVariable Long id) {
        BaseResponse<TestVo> response = new BaseResponse<>();
        try {
            TestVo testVo = testService.findTestById(id);
            response.setResData(testVo);
        } catch (Exception e) {
            e.printStackTrace();
            response.setResCode(ResCodeEnum.FAIL);
        }
        return response;
    }

    @PutMapping("/updateTestById/{id}")
    public BaseResponse<TestVo> updateTestById(@PathVariable Long id) {
        BaseResponse<TestVo> response = new BaseResponse<>();
        try {
            TestVo testVo = testService.updateTestById(id);
            response.setResData(testVo);
        } catch (Exception e) {
            e.printStackTrace();
            response.setResCode(ResCodeEnum.FAIL);
        }
        return response;
    }

    @DeleteMapping("/deleteTestById/{id}")
    public BaseResponse<TestVo> deleteTestById(@PathVariable Long id) {
        BaseResponse<TestVo> response = new BaseResponse<>();
        try {
            testService.deleteTestById(id);
        } catch (Exception e) {
            e.printStackTrace();
            response.setResCode(ResCodeEnum.FAIL);
        }
        return response;
    }

    @GetMapping("/getTest")
    public BaseResponse<List<WhiteListVo>> getTest() {
        WhiteListVo vo = new WhiteListVo();
        vo.setIp("127.0.0.1").setModule("learn-shop-core-product").setValidInd(true);
        String ip = "127.0.0.1";
        String module = "learn-shop-core-product";
        boolean validInd = true;
        BaseResponse<List<WhiteListVo>> baseRes = adminSystemRemote.findWhiteListVos(ip, module, validInd);
        return baseRes;
    }
}