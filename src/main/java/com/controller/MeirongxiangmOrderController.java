
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
 * 美容项目订单
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/meirongxiangmOrder")
public class MeirongxiangmOrderController {
    private static final Logger logger = LoggerFactory.getLogger(MeirongxiangmOrderController.class);

    private static final String TABLE_NAME = "meirongxiangmOrder";

    @Autowired
    private MeirongxiangmOrderService meirongxiangmOrderService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private MeirongshiService meirongshiService;//美容师
    @Autowired
    private MeirongshiCollectionService meirongshiCollectionService;//美容师收藏
    @Autowired
    private MeirongshiLiuyanService meirongshiLiuyanService;//美容师留言
    @Autowired
    private MeirongshiOrderService meirongshiOrderService;//美容师预约
    @Autowired
    private MeirongxiangmService meirongxiangmService;//美容项目
    @Autowired
    private MeirongxiangmCollectionService meirongxiangmCollectionService;//美容项目收藏
    @Autowired
    private MeirongxiangmLiuyanService meirongxiangmLiuyanService;//美容项目留言
    @Autowired
    private NewsService newsService;//公告通知
    @Autowired
    private YonghuService yonghuService;//用户
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
        else if("美容师".equals(role))
            params.put("meirongshiId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = meirongxiangmOrderService.queryPage(params);

        //字典表数据转换
        List<MeirongxiangmOrderView> list =(List<MeirongxiangmOrderView>)page.getList();
        for(MeirongxiangmOrderView c:list){
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
        MeirongxiangmOrderEntity meirongxiangmOrder = meirongxiangmOrderService.selectById(id);
        if(meirongxiangmOrder !=null){
            //entity转view
            MeirongxiangmOrderView view = new MeirongxiangmOrderView();
            BeanUtils.copyProperties( meirongxiangmOrder , view );//把实体数据重构到view中
            //级联表 美容项目
            //级联表
            MeirongxiangmEntity meirongxiangm = meirongxiangmService.selectById(meirongxiangmOrder.getMeirongxiangmId());
            if(meirongxiangm != null){
            BeanUtils.copyProperties( meirongxiangm , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setMeirongxiangmId(meirongxiangm.getId());
            }
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(meirongxiangmOrder.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
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
    public R save(@RequestBody MeirongxiangmOrderEntity meirongxiangmOrder, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,meirongxiangmOrder:{}",this.getClass().getName(),meirongxiangmOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            meirongxiangmOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        meirongxiangmOrder.setCreateTime(new Date());
        meirongxiangmOrder.setInsertTime(new Date());
        meirongxiangmOrderService.insert(meirongxiangmOrder);

        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody MeirongxiangmOrderEntity meirongxiangmOrder, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,meirongxiangmOrder:{}",this.getClass().getName(),meirongxiangmOrder.toString());
        MeirongxiangmOrderEntity oldMeirongxiangmOrderEntity = meirongxiangmOrderService.selectById(meirongxiangmOrder.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            meirongxiangmOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

            meirongxiangmOrderService.updateById(meirongxiangmOrder);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<MeirongxiangmOrderEntity> oldMeirongxiangmOrderList =meirongxiangmOrderService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        meirongxiangmOrderService.deleteBatchIds(Arrays.asList(ids));

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
            List<MeirongxiangmOrderEntity> meirongxiangmOrderList = new ArrayList<>();//上传的东西
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
                            MeirongxiangmOrderEntity meirongxiangmOrderEntity = new MeirongxiangmOrderEntity();
//                            meirongxiangmOrderEntity.setMeirongxiangmOrderUuidNumber(data.get(0));                    //订单编号 要改的
//                            meirongxiangmOrderEntity.setMeirongxiangmId(Integer.valueOf(data.get(0)));   //美容项目 要改的
//                            meirongxiangmOrderEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            meirongxiangmOrderEntity.setMeirongxiangmOrderTime(sdf.parse(data.get(0)));          //预约时间 要改的
//                            meirongxiangmOrderEntity.setMeirongxiangmOrderTruePrice(data.get(0));                    //实付价格 要改的
//                            meirongxiangmOrderEntity.setMeirongxiangmOrderTypes(Integer.valueOf(data.get(0)));   //订单类型 要改的
//                            meirongxiangmOrderEntity.setMeirongxiangmOrderPaymentTypes(Integer.valueOf(data.get(0)));   //支付类型 要改的
//                            meirongxiangmOrderEntity.setInsertTime(date);//时间
//                            meirongxiangmOrderEntity.setCreateTime(date);//时间
                            meirongxiangmOrderList.add(meirongxiangmOrderEntity);


                            //把要查询是否重复的字段放入map中
                                //订单编号
                                if(seachFields.containsKey("meirongxiangmOrderUuidNumber")){
                                    List<String> meirongxiangmOrderUuidNumber = seachFields.get("meirongxiangmOrderUuidNumber");
                                    meirongxiangmOrderUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> meirongxiangmOrderUuidNumber = new ArrayList<>();
                                    meirongxiangmOrderUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("meirongxiangmOrderUuidNumber",meirongxiangmOrderUuidNumber);
                                }
                        }

                        //查询是否重复
                         //订单编号
                        List<MeirongxiangmOrderEntity> meirongxiangmOrderEntities_meirongxiangmOrderUuidNumber = meirongxiangmOrderService.selectList(new EntityWrapper<MeirongxiangmOrderEntity>().in("meirongxiangm_order_uuid_number", seachFields.get("meirongxiangmOrderUuidNumber")));
                        if(meirongxiangmOrderEntities_meirongxiangmOrderUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(MeirongxiangmOrderEntity s:meirongxiangmOrderEntities_meirongxiangmOrderUuidNumber){
                                repeatFields.add(s.getMeirongxiangmOrderUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [订单编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        meirongxiangmOrderService.insertBatch(meirongxiangmOrderList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = meirongxiangmOrderService.queryPage(params);

        //字典表数据转换
        List<MeirongxiangmOrderView> list =(List<MeirongxiangmOrderView>)page.getList();
        for(MeirongxiangmOrderView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Integer id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        MeirongxiangmOrderEntity meirongxiangmOrder = meirongxiangmOrderService.selectById(id);
            if(meirongxiangmOrder !=null){


                //entity转view
                MeirongxiangmOrderView view = new MeirongxiangmOrderView();
                BeanUtils.copyProperties( meirongxiangmOrder , view );//把实体数据重构到view中

                //级联表
                    MeirongxiangmEntity meirongxiangm = meirongxiangmService.selectById(meirongxiangmOrder.getMeirongxiangmId());
                if(meirongxiangm != null){
                    BeanUtils.copyProperties( meirongxiangm , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setMeirongxiangmId(meirongxiangm.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(meirongxiangmOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "username", "password", "newMoney", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody MeirongxiangmOrderEntity meirongxiangmOrder, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,meirongxiangmOrder:{}",this.getClass().getName(),meirongxiangmOrder.toString());
            MeirongxiangmEntity meirongxiangmEntity = meirongxiangmService.selectById(meirongxiangmOrder.getMeirongxiangmId());
            if(meirongxiangmEntity == null){
                return R.error(511,"查不到该美容项目");
            }
            // Double meirongxiangmNewMoney = meirongxiangmEntity.getMeirongxiangmNewMoney();

            if(false){
            }
            else if(meirongxiangmEntity.getMeirongxiangmNewMoney() == null){
                return R.error(511,"价格不能为空");
            }

            //计算所获得积分
            Double buyJifen =0.0;
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
                return R.error(511,"用户金额不能为空");
            double balance = yonghuEntity.getNewMoney() - meirongxiangmEntity.getMeirongxiangmNewMoney()*1;//余额
            buyJifen = new BigDecimal(meirongxiangmEntity.getMeirongxiangmPrice()).multiply(new BigDecimal(1)).doubleValue();//所获积分
            if(balance<0)
                return R.error(511,"余额不够支付");
            meirongxiangmOrder.setMeirongxiangmOrderTypes(101); //设置订单状态为已支付
            meirongxiangmOrder.setMeirongxiangmOrderTruePrice(meirongxiangmEntity.getMeirongxiangmNewMoney()*1); //设置实付价格
            meirongxiangmOrder.setYonghuId(userId); //设置订单支付人id
            meirongxiangmOrder.setMeirongxiangmOrderUuidNumber(String.valueOf(new Date().getTime()));
            meirongxiangmOrder.setMeirongxiangmOrderPaymentTypes(1);
            meirongxiangmOrder.setInsertTime(new Date());
            meirongxiangmOrder.setCreateTime(new Date());
                meirongxiangmOrderService.insert(meirongxiangmOrder);//新增订单
            //更新第一注册表
            yonghuEntity.setNewMoney(balance);//设置金额
                yonghuEntity.setYonghuNewJifen(yonghuEntity.getYonghuNewJifen() + buyJifen); //设置总积分
            yonghuService.updateById(yonghuEntity);


            return R.ok();
    }


    /**
    * 退款
    */
    @RequestMapping("/refund")
    public R refund(Integer id, HttpServletRequest request){
        logger.debug("refund方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        String role = String.valueOf(request.getSession().getAttribute("role"));

            MeirongxiangmOrderEntity meirongxiangmOrder = meirongxiangmOrderService.selectById(id);//当前表service
            Integer meirongxiangmOrderPaymentTypes = meirongxiangmOrder.getMeirongxiangmOrderPaymentTypes();
            Integer meirongxiangmId = meirongxiangmOrder.getMeirongxiangmId();
            if(meirongxiangmId == null)
                return R.error(511,"查不到该美容项目");
            MeirongxiangmEntity meirongxiangmEntity = meirongxiangmService.selectById(meirongxiangmId);
            if(meirongxiangmEntity == null)
                return R.error(511,"查不到该美容项目");
            Double meirongxiangmNewMoney = meirongxiangmEntity.getMeirongxiangmNewMoney();
            if(meirongxiangmNewMoney == null)
                return R.error(511,"美容项目价格不能为空");

            Integer userId = (Integer) request.getSession().getAttribute("userId");
            YonghuEntity yonghuEntity = yonghuService.selectById(userId);
            if(yonghuEntity == null)
                return R.error(511,"用户不能为空");
            if(yonghuEntity.getNewMoney() == null)
            return R.error(511,"用户金额不能为空");
            Double zhekou = 1.0;
            // 获取折扣
            Wrapper<DictionaryEntity> dictionary = new EntityWrapper<DictionaryEntity>()
                    .eq("dic_code", "huiyuandengji_types")
                    .eq("dic_name", "会员等级类型")
                    .eq("code_index", yonghuEntity.getHuiyuandengjiTypes())
                    ;
            DictionaryEntity dictionaryEntity = dictionaryService.selectOne(dictionary);
            if(dictionaryEntity != null ){
                zhekou = Double.valueOf(dictionaryEntity.getBeizhu());
            }

            //判断是什么支付方式 1代表余额 2代表积分
            if(meirongxiangmOrderPaymentTypes == 1){//余额支付
                //计算金额
                Double money = meirongxiangmEntity.getMeirongxiangmNewMoney() * 1  * zhekou;
                //计算所获得积分
                Double buyJifen = 0.0;
                buyJifen = new BigDecimal(meirongxiangmEntity.getMeirongxiangmPrice()).multiply(new BigDecimal(1)).doubleValue();
                yonghuEntity.setNewMoney(yonghuEntity.getNewMoney() + money); //设置金额
                if(yonghuEntity.getYonghuNewJifen() - buyJifen <0 )
                    return R.error("积分已经消费,无法退款！！！");
                yonghuEntity.setYonghuNewJifen(yonghuEntity.getYonghuNewJifen() - buyJifen); //设置现积分


            }


            meirongxiangmOrder.setMeirongxiangmOrderTypes(102);//设置订单状态为已退款
            meirongxiangmOrderService.updateAllColumnById(meirongxiangmOrder);//根据id更新
            yonghuService.updateById(yonghuEntity);//更新用户信息
            meirongxiangmService.updateById(meirongxiangmEntity);//更新订单中美容项目的信息

            return R.ok();
    }

    /**
     * 完成
     */
    @RequestMapping("/deliver")
    public R deliver(Integer id  , HttpServletRequest request){
        logger.debug("refund:,,Controller:{},,ids:{}",this.getClass().getName(),id.toString());
        MeirongxiangmOrderEntity  meirongxiangmOrderEntity = meirongxiangmOrderService.selectById(id);
        meirongxiangmOrderEntity.setMeirongxiangmOrderTypes(103);//设置订单状态为已完成
        meirongxiangmOrderService.updateById( meirongxiangmOrderEntity);

        return R.ok();
    }


}

