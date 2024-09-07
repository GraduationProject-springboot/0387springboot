
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 材料验收
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/yanshouCailiao")
public class YanshouCailiaoController {
    private static final Logger logger = LoggerFactory.getLogger(YanshouCailiaoController.class);

    private static final String TABLE_NAME = "yanshouCailiao";

    @Autowired
    private YanshouCailiaoService yanshouCailiaoService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private CailiaocankaoService cailiaocankaoService;//材料参考
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private JianzaocaiwuService jianzaocaiwuService;//监造财务
    @Autowired
    private JianzaojinduService jianzaojinduService;//监造进度
    @Autowired
    private JingfeiyuguService jingfeiyuguService;//经费预估
    @Autowired
    private XiangmuService xiangmuService;//项目
    @Autowired
    private XiangmuTuzhiService xiangmuTuzhiService;//项目图纸
    @Autowired
    private YanshouChengguoService yanshouChengguoService;//成果验收
    @Autowired
    private YanshouGongyiService yanshouGongyiService;//工艺验收
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private YuangongService yuangongService;//员工
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("员工".equals(role))
            params.put("yuangongId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = yanshouCailiaoService.queryPage(params);

        //字典表数据转换
        List<YanshouCailiaoView> list =(List<YanshouCailiaoView>)page.getList();
        for(YanshouCailiaoView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YanshouCailiaoEntity yanshouCailiao = yanshouCailiaoService.selectById(id);
        if(yanshouCailiao !=null){
            //entity转view
            YanshouCailiaoView view = new YanshouCailiaoView();
            BeanUtils.copyProperties( yanshouCailiao , view );//把实体数据重构到view中
            //级联表 项目
            //级联表
            XiangmuEntity xiangmu = xiangmuService.selectById(yanshouCailiao.getXiangmuId());
            if(xiangmu != null){
            BeanUtils.copyProperties( xiangmu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yuangongId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setXiangmuId(xiangmu.getId());
            }
            //级联表 员工
            //级联表
            YuangongEntity yuangong = yuangongService.selectById(yanshouCailiao.getYuangongId());
            if(yuangong != null){
            BeanUtils.copyProperties( yuangong , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yuangongId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYuangongId(yuangong.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YanshouCailiaoEntity yanshouCailiao, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,yanshouCailiao:{}",this.getClass().getName(),yanshouCailiao.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("员工".equals(role))
            yanshouCailiao.setYuangongId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<YanshouCailiaoEntity> queryWrapper = new EntityWrapper<YanshouCailiaoEntity>()
            .eq("xiangmu_id", yanshouCailiao.getXiangmuId())
            .eq("yuangong_id", yanshouCailiao.getYuangongId())
            .eq("yanshou_cailiao_name", yanshouCailiao.getYanshouCailiaoName())
            .eq("yanshou_cailiao_buwei", yanshouCailiao.getYanshouCailiaoBuwei())
            .eq("yanshou_cailiao_biaozhun", yanshouCailiao.getYanshouCailiaoBiaozhun())
            .eq("yanshou_cailiao_shijishiyong", yanshouCailiao.getYanshouCailiaoShijishiyong())
            .eq("hege_types", yanshouCailiao.getHegeTypes())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YanshouCailiaoEntity yanshouCailiaoEntity = yanshouCailiaoService.selectOne(queryWrapper);
        if(yanshouCailiaoEntity==null){
            yanshouCailiao.setInsertTime(new Date());
            yanshouCailiao.setCreateTime(new Date());
            yanshouCailiaoService.insert(yanshouCailiao);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YanshouCailiaoEntity yanshouCailiao, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,yanshouCailiao:{}",this.getClass().getName(),yanshouCailiao.toString());
        YanshouCailiaoEntity oldYanshouCailiaoEntity = yanshouCailiaoService.selectById(yanshouCailiao.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("员工".equals(role))
//            yanshouCailiao.setYuangongId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        if("".equals(yanshouCailiao.getJianzaojinduFile()) || "null".equals(yanshouCailiao.getJianzaojinduFile())){
                yanshouCailiao.setJianzaojinduFile(null);
        }
        if("".equals(yanshouCailiao.getYanshouCailiaoContent()) || "null".equals(yanshouCailiao.getYanshouCailiaoContent())){
                yanshouCailiao.setYanshouCailiaoContent(null);
        }

            yanshouCailiaoService.updateById(yanshouCailiao);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<YanshouCailiaoEntity> oldYanshouCailiaoList =yanshouCailiaoService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        yanshouCailiaoService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<YanshouCailiaoEntity> yanshouCailiaoList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            YanshouCailiaoEntity yanshouCailiaoEntity = new YanshouCailiaoEntity();
//                            yanshouCailiaoEntity.setXiangmuId(Integer.valueOf(data.get(0)));   //项目 要改的
//                            yanshouCailiaoEntity.setYuangongId(Integer.valueOf(data.get(0)));   //员工 要改的
//                            yanshouCailiaoEntity.setYanshouCailiaoName(data.get(0));                    //材料名称 要改的
//                            yanshouCailiaoEntity.setYanshouCailiaoBuwei(data.get(0));                    //监造部位 要改的
//                            yanshouCailiaoEntity.setYanshouCailiaoBiaozhun(data.get(0));                    //使用标准 要改的
//                            yanshouCailiaoEntity.setYanshouCailiaoShijishiyong(data.get(0));                    //实际使用 要改的
//                            yanshouCailiaoEntity.setJianzaojinduFile(data.get(0));                    //附件 要改的
//                            yanshouCailiaoEntity.setHegeTypes(Integer.valueOf(data.get(0)));   //是否合格 要改的
//                            yanshouCailiaoEntity.setYanshouTime(sdf.parse(data.get(0)));          //验收时间 要改的
//                            yanshouCailiaoEntity.setYanshouCailiaoContent("");//详情和图片
//                            yanshouCailiaoEntity.setInsertTime(date);//时间
//                            yanshouCailiaoEntity.setCreateTime(date);//时间
                            yanshouCailiaoList.add(yanshouCailiaoEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        yanshouCailiaoService.insertBatch(yanshouCailiaoList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




}

