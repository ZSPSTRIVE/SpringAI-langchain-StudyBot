// 动态创建并插入新的CSS样式
var style = document.createElement('style');
style.innerHTML = `
/* 全局样式，去除多余的元素和默认样式 */
body {
    margin: 0;
    padding: 0;
    font-family: 'Arial', sans-serif;
    background-color: #f7f7f7; /* 背景色 */
}

/* 设置页面顶部的标题样式 */
#projectTitle {
    position: fixed;                /* 固定在页面顶部 */
    top: 20px;                      /* 离顶部一定的距离 */
    left: 50%;                      /* 从左边开始居中 */
    transform: translateX(-50%);    /* 通过translateX(-50%)实现水平居中 */
    font-size: 36px;                /* 设置字体大小 */
    font-weight: 700;               /* 设置字体为加粗 */
    color: transparent;             /* 文字透明，待渐变背景填充 */
    padding: 15px 25px;             /* 给标题添加一些内边距 */
    border-radius: 15px;            /* 添加圆角 */
    background: linear-gradient(135deg, #6a82fb, #fc5c7d); /* 初始渐变背景 */
    -webkit-background-clip: text;  /* 使背景渐变只应用于文字 */
    background-clip: text;          /* 使背景渐变只应用于文字 */
    opacity: 0;                     /* 初始透明度为0，用于动画效果 */
    animation: fadeIn 2s forwards, textColorShift 3s infinite alternate; /* 渐显 + 颜色切换 */
    text-align: center;
}

/* 动画定义：渐隐渐显 */
@keyframes fadeIn {
    0% {
        opacity: 0;
        transform: translateX(-50%) scale(0.5);
    }
    100% {
        opacity: 1;
        transform: translateX(-50%) scale(1);
    }
}

/* 动画定义：字体颜色切换 */
@keyframes textColorShift {
    0% {
        background: linear-gradient(135deg, #6a82fb, #fc5c7d);
    }
    33% {
        background: linear-gradient(135deg, #fc5c7d, #f7b42c);
    }
    66% {
        background: linear-gradient(135deg, #f7b42c, #43e97b);
    }
    100% {
        background: linear-gradient(135deg, #43e97b, #6a82fb);
    }
}

/* 页面内容区，避免标题遮挡内容 */
.content {
    padding-top: 100px;  /* 确保标题不与内容重叠 */
    text-align: center;
    font-size: 18px;
    color: #333;
    line-height: 1.6;
}

/* 清除滚动条 */
body::-webkit-scrollbar {
    display: none;
}

`;

// 将样式插入到页面的<head>标签中
document.head.appendChild(style);

// 确保只插入一个标题，避免重复插入
if (!document.querySelector('#projectTitle')) {
    // 动态创建标题并插入到页面
    var projectName = '大学课程师生答疑系统';

    // 创建一个div元素用来显示新的标题
    var titleElement = document.createElement('div');
    titleElement.id = 'projectTitle'; // 设置ID，便于CSS控制
    titleElement.innerText = projectName;

//     // 将标题元素插入到页面的body中
//     // document.body.appendChild(titleElement);
// } else {
//     // console.log("标题已存在，未重复插入");
// }

// 创建一个div元素，作为页面的内容区域
var contentElement = document.createElement('div');
contentElement.classList.add('content');

document.body.appendChild(contentElement);



/**
 * 轮播图配置
 */
var swiper = {
	// 设定轮播容器宽度，支持像素和百分比
	width: '100%',
	height: '400px',
	// hover（悬停显示）
	// always（始终显示）
	// none（始终不显示）
	arrow: 'none',
	// default（左右切换）
	// updown（上下切换）
	// fade（渐隐渐显切换）
	anim: 'default',

	interval: 1000,
	// 指示器位置
	// inside（容器内部）
	// outside（容器外部）
	// none（不显示）
	indicator: 'outside'
}

/**
 * 个人中心菜单
 */
var centerMenu = [{
	name: '个人中心',
	url: '../' + localStorage.getItem('userTable') + '/center.html'
},
{
	name: '我的发布',
	url: '../forum/list-my.html'
},

{
	name: '我的收藏',
	url: '../storeup/list.html'
}
]


var indexNav = [

{
	name: '科目问题',
	url: './pages/xueshengwenti/list.html'
},
{
	name: '老师回答',
	url: './pages/laoshihuida/list.html'
},
{
	name: '老师信息',
	url: './pages/laoshixinxi/list.html'
},

{
	name: '互助区',
	url: './pages/forum/list.html'
},
]

var adminurl =  "/springboot7vkr1/admin/dist/index.html";

var cartFlag = false

var chatFlag = false




var menu = [{"backMenu":[{"child":[{"buttons":["新增","查看","修改","删除"],"menu":"学生","menuJump":"列表","tableName":"xuesheng"}],"menu":"学生管理"},{"child":[{"buttons":["新增","查看","修改","删除"],"menu":"老师","menuJump":"列表","tableName":"laoshi"}],"menu":"老师管理"},{"child":[{"buttons":["新增","查看","修改","删除"],"menu":"科目类型","menuJump":"列表","tableName":"kemuleixing"}],"menu":"科目类型管理"},{"child":[{"buttons":["查看","修改","删除","查看评论"],"menu":"科目问题","menuJump":"列表","tableName":"xueshengwenti"}],"menu":"科目问题管理"},{"child":[{"buttons":["查看","修改","删除","查看评论"],"menu":"老师回答","menuJump":"列表","tableName":"laoshihuida"}],"menu":"老师回答管理"},{"child":[{"buttons":["查看","修改","删除","查看评论"],"menu":"老师信息","menuJump":"列表","tableName":"laoshixinxi"}],"menu":"老师信息管理"},{"child":[{"buttons":["查看","修改","删除"],"menu":"关注列表","menuJump":"列表","tableName":"guanzhuliebiao"}],"menu":"关注列表管理"},{"child":[{"buttons":["查看","修改","删除"],"menu":"互助区","tableName":"forum"}],"menu":"互助区"},{"child":[{"buttons":["查看","修改"],"menu":"轮播图管理","tableName":"config"}],"menu":"系统管理"}],"frontMenu":[{"child":[{"buttons":["查看","回答"],"menu":"科目问题列表","menuJump":"列表","tableName":"xueshengwenti"}],"menu":"科目问题模块"},{"child":[{"buttons":["查看"],"menu":"老师回答列表","menuJump":"列表","tableName":"laoshihuida"}],"menu":"老师回答模块"},{"child":[{"buttons":["查看","关注"],"menu":"老师信息列表","menuJump":"列表","tableName":"laoshixinxi"}],"menu":"老师信息模块"}],"hasBackLogin":"是","hasBackRegister":"否","hasFrontLogin":"否","hasFrontRegister":"否","roleName":"管理员","tableName":"users"},{"backMenu":[{"child":[{"buttons":["新增","查看"],"menu":"科目类型","menuJump":"列表","tableName":"kemuleixing"}],"menu":"科目类型管理"},{"child":[{"buttons":["查看","新增","修改","删除"],"menu":"科目问题","menuJump":"列表","tableName":"xueshengwenti"}],"menu":"科目问题管理"},{"child":[{"buttons":["查看"],"menu":"老师回答","menuJump":"列表","tableName":"laoshihuida"}],"menu":"老师回答管理"},{"child":[{"buttons":["查看","关注"],"menu":"老师信息","menuJump":"列表","tableName":"laoshixinxi"}],"menu":"老师信息管理"},{"child":[{"buttons":["查看"],"menu":"关注列表","menuJump":"列表","tableName":"guanzhuliebiao"}],"menu":"关注列表管理"},{"child":[{"buttons":["查看","修改","删除"],"menu":"我的收藏管理","tableName":"storeup"}],"menu":"我的收藏管理"}],"frontMenu":[{"child":[{"buttons":["查看","回答"],"menu":"科目问题列表","menuJump":"列表","tableName":"xueshengwenti"}],"menu":"科目问题模块"},{"child":[{"buttons":["查看"],"menu":"老师回答列表","menuJump":"列表","tableName":"laoshihuida"}],"menu":"老师回答模块"},{"child":[{"buttons":["查看","关注"],"menu":"老师信息列表","menuJump":"列表","tableName":"laoshixinxi"}],"menu":"老师信息模块"}],"hasBackLogin":"是","hasBackRegister":"是","hasFrontLogin":"是","hasFrontRegister":"是","roleName":"学生","tableName":"xuesheng"},{"backMenu":[{"child":[{"buttons":["查看"],"menu":"老师回答","menuJump":"列表","tableName":"laoshihuida"}],"menu":"老师回答管理"},{"child":[{"buttons":["新增","修改","删除","查看"],"menu":"老师信息","menuJump":"列表","tableName":"laoshixinxi"}],"menu":"老师信息管理"},{"child":[{"buttons":["查看"],"menu":"关注列表","menuJump":"列表","tableName":"guanzhuliebiao"}],"menu":"关注列表管理"},{"child":[{"buttons":["查看","修改","删除"],"menu":"我的收藏管理","tableName":"storeup"}],"menu":"我的收藏管理"}],"frontMenu":[{"child":[{"buttons":["查看","回答"],"menu":"科目问题列表","menuJump":"列表","tableName":"xueshengwenti"}],"menu":"科目问题模块"},{"child":[{"buttons":["查看"],"menu":"老师回答列表","menuJump":"列表","tableName":"laoshihuida"}],"menu":"老师回答模块"},{"child":[{"buttons":["查看","关注"],"menu":"老师信息列表","menuJump":"列表","tableName":"laoshixinxi"}],"menu":"老师信息模块"}],"hasBackLogin":"是","hasBackRegister":"是","hasFrontLogin":"是","hasFrontRegister":"是","roleName":"老师","tableName":"laoshi"}]


var isAuth = function (tableName,key) {
    let role = localStorage.getItem("userTable");
    let menus = menu;
    for(let i=0;i<menus.length;i++){
        if(menus[i].tableName==role){
            for(let j=0;j<menus[i].backMenu.length;j++){
                for(let k=0;k<menus[i].backMenu[j].child.length;k++){
                    if(tableName==menus[i].backMenu[j].child[k].tableName){
                        let buttons = menus[i].backMenu[j].child[k].buttons.join(',');
                        return buttons.indexOf(key) !== -1 || false
                    }
                }
            }
        }
    }
    return false;
}

var isFrontAuth = function (tableName,key) {
    let role = localStorage.getItem("userTable");
    let menus = menu;
    for(let i=0;i<menus.length;i++){
        if(menus[i].tableName==role){
            for(let j=0;j<menus[i].frontMenu.length;j++){
                for(let k=0;k<menus[i].frontMenu[j].child.length;k++){
                    if(tableName==menus[i].frontMenu[j].child[k].tableName){
                        let buttons = menus[i].frontMenu[j].child[k].buttons.join(',');
                        return buttons.indexOf(key) !== -1 || false
                    }
                }
            }
        }
    }
    return false;
}}
