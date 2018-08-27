var VueUtils = {
  // /* ================ 深拷贝,不含有 function 可用 ================ */
  deepCloneJson: function (initalObj) {
    var obj = {};
    try {
      obj = JSON.parse(JSON.stringify(initalObj));
    } catch (e) {
      console.error(e);
    }
    return obj;
  },

  /* ================ 深拷贝 ================ */
  deepClone: function deepClone(initalObj, finalObj) {
    // var obj = finalObj || {};
    // for (var i in initalObj) {
    //   var prop = initalObj[i];
    //   // 避免相互引用对象导致死循环，如initalObj.a = initalObj的情况
    //   if (prop === obj) {
    //     continue;
    //   }
    //   if (prop != null && typeof prop === 'object') {
    //     obj[i] = (prop.constructor === Array) ? [] : Object.create(prop);
    //   } else {
    //     obj[i] = prop;
    //   }
    // }
    // return obj;
    return this.deepClone3(initalObj);
  },
  deepClone2: function deepClone(obj) {
    //声明一个变量data用来保存深复制后的数据
    let data;
    //判断该参数是否存在且是Object类型
    if (obj && typeof obj === Object) {
      //声明一个变量,并判断他是array还是object
      data = Array.isArray(obj) ? [] : {};
      //便利该对象
      for (key in obj) {
        //如果该obj的孩子也是一个对象，那么递归调用deepClone
        if (typeof key === Object) {
          data[key] = deepClone(obj[key])
        } else {
          data[key] = obj[key]
        }
      }

    } else {
      data = obj;
    }
    return data;
  },
  deepClone3: function deepClone(obj) {
    if (!obj) {
      return obj;
    }
    let sourceCopy = obj instanceof Array ? [] : {};
    for (let item in obj) {
      sourceCopy[item] = typeof obj[item] === 'object' ? deepClone(obj[item]) : obj[item]
    }
    return sourceCopy;
  },
  /**
   * 元素进行类型判断
   * @param 元素
   * @returns 元素类型
   */
  getType: function (obj) {
    //tostring会返回对应不同的标签的构造函数
    var toString = Object.prototype.toString;
    var map = {
      '[object Boolean]': 'boolean',
      '[object Number]': 'number',
      '[object String]': 'string',
      '[object Function]': 'function',
      '[object Array]': 'array',
      '[object Date]': 'date',
      '[object RegExp]': 'regExp',
      '[object Undefined]': 'undefined',
      '[object Null]': 'null',
      '[object Object]': 'object'
    };
    if (obj instanceof Element) {
      return 'element';
    }
    return map[toString.call(obj)];
  }
}

export default VueUtils
