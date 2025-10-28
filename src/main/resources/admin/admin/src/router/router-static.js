import Vue from 'vue';
//配置路由
import VueRouter from 'vue-router'
Vue.use(VueRouter);
//1.创建组件
import Index from '@/views/index'
import Home from '@/views/home'
import Login from '@/views/login'
import NotFound from '@/views/404'
import UpdatePassword from '@/views/update-password'
import pay from '@/views/pay'
import register from '@/views/register'
import center from '@/views/center'
    import discussxueshengwenti from '@/views/modules/discussxueshengwenti/list'
    import xuesheng from '@/views/modules/xuesheng/list'
    import guanzhuliebiao from '@/views/modules/guanzhuliebiao/list'
    import discusslaoshixinxi from '@/views/modules/discusslaoshixinxi/list'
    import discusslaoshihuida from '@/views/modules/discusslaoshihuida/list'
    import laoshi from '@/views/modules/laoshi/list'
    import storeup from '@/views/modules/storeup/list'
    import kemuleixing from '@/views/modules/kemuleixing/list'
    import xueshengwenti from '@/views/modules/xueshengwenti/list'
    import forum from '@/views/modules/forum/list'
    import laoshixinxi from '@/views/modules/laoshixinxi/list'
    import laoshihuida from '@/views/modules/laoshihuida/list'
    import config from '@/views/modules/config/list'


//2.配置路由   注意：名字
const routes = [{
    path: '/index',
    name: '首页',
    component: Index,
    children: [{
      // 这里不设置值，是把main作为默认页面
      path: '/',
      name: '首页',
      component: Home,
      meta: {icon:'', title:'center'}
    }, {
      path: '/updatePassword',
      name: '修改密码',
      component: UpdatePassword,
      meta: {icon:'', title:'updatePassword'}
    }, {
      path: '/pay',
      name: '支付',
      component: pay,
      meta: {icon:'', title:'pay'}
    }, {
      path: '/center',
      name: '个人信息',
      component: center,
      meta: {icon:'', title:'center'}
    }
      ,{
	path: '/discussxueshengwenti',
        name: '科目问题评论',
        component: discussxueshengwenti
      }
      ,{
	path: '/xuesheng',
        name: '学生',
        component: xuesheng
      }
      ,{
	path: '/guanzhuliebiao',
        name: '关注列表',
        component: guanzhuliebiao
      }
      ,{
	path: '/discusslaoshixinxi',
        name: '任课老师评论',
        component: discusslaoshixinxi
      }
      ,{
	path: '/discusslaoshihuida',
        name: '老师回答评论',
        component: discusslaoshihuida
      }
      ,{
	path: '/laoshi',
        name: '老师',
        component: laoshi
      }
      ,{
	path: '/storeup',
        name: '我的收藏管理',
        component: storeup
      }
      ,{
	path: '/kemuleixing',
        name: '科目类型',
        component: kemuleixing
      }
      ,{
	path: '/xueshengwenti',
        name: '科目问题',
        component: xueshengwenti
      }
      ,{
	path: '/forum',
        name: '互助区',
        component: forum
      }
      ,{
	path: '/laoshixinxi',
        name: '任课老师',
        component: laoshixinxi
      }
      ,{
	path: '/laoshihuida',
        name: '老师回答',
        component: laoshihuida
      }
      ,{
	path: '/config',
        name: '轮播图',
        component: config
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: {icon:'', title:'login'}
  },
  {
    path: '/register',
    name: 'register',
    component: register,
    meta: {icon:'', title:'register'}
  },
  {
    path: '/',
    name: '首页',
    redirect: '/index'
  }, /*默认跳转路由*/
  {
    path: '*',
    component: NotFound
  }
]
//3.实例化VueRouter  注意：名字
const router = new VueRouter({
  mode: 'hash',
  /*hash模式改为history*/
  routes // （缩写）相当于 routes: routes
})

export default router;
