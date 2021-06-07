const ApiResponse = function (code, data, message) {
    this.code = code;
    this.data = data;
    this.message = message;
    this.isSuccessful = function () {
        return this.code === 200;
    }
}

const AxiosEngine = function (sdk) {
    this.requests = [];
    this.isRefreshing = false;
    this.token = "";
    this.axios = axios.create({
        baseURL: "https://wallet.codbtoken.com/api",
    });
    this.axios.interceptors.request.use(
        (config) => {
            config.headers["token"] = this.token
            if (config.method === "POST" && config.data && config.url !== "/platform/token") {
                config.data = this.encrypt(sdk.secretKey, JSON.stringify(config.data))
            }
            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );
    this.axios.interceptors.response.use(
        (response) => {
            const data = response.data;
            const result = new ApiResponse(data.code, data.data, data.message);
            if (response.config.url !== "/platform/token") {
                if (data.code === 200) {
                    result.data = JSON.parse(this.decrypt(sdk.secretKey, data.data))
                }
                return result;
            }
            return result;
        },
        (error) => {
            if (error.response.status === 401) {
                //https://juejin.cn/post/6844903925078818829
                //https://my.oschina.net/kisshua/blog/2989500https://my.oschina.net/kisshua/blog/2989500
                let config = error.config
                if (!this.isRefreshing) {
                    this.isRefreshing = true;
                    sdk.api.updateToken()
                    this.requests.forEach(cb => cb())
                    this.requests = [];
                    //重新发起axios请求
                    return new Promise(resolve => {
                        setTimeout(() => {
                            resolve();
                        }, 1000);
                    }).then(() => this.axios(config));
                } else {
                    return new Promise(resolve => {
                        this.requests.push(() => {
                            resolve(this.axios(config));
                        })
                    });
                }
            }
            return Promise.reject(error);
        }
    )
    this.encrypt = function (key, content) {
        return CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(content), CryptoJS.enc.Utf8.parse(key), {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7}).toString();
    }
    this.decrypt = function (key, content) {
        return CryptoJS.enc.Utf8.stringify(CryptoJS.AES.decrypt(content, CryptoJS.enc.Utf8.parse(key), {mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7})).toString();
    }
    this.httpGet = function (path, params) {
        return this.axios({
            url: path,
            method: 'GET',
            params: params
        });
    }
    this.httpPostJson = function (path, data) {
        return this.axios({
            url: path,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            data: data
        });
    }
}

const Api = function (sdk, axios) {
    this.updateToken = function () {
        const data = {"no": sdk.no, "accessKey": sdk.accessKey};
        axios.httpPostJson("/platform/token", data).then((response) => {
            if (response.isSuccessful()) {
                axios.token = response.data.token;
            }
        }).finally(() => {
            axios.isRefreshing = false;
        });
    }

    /**
     * 发布资产
     * @param symbol 资产名（只能使用字母）
     * @param name 资产别名（可以用中文0
     * @param total 发布数量
     * @param logo 资产logo
     * @param introduce 资产介绍
     * @param whiteBook 白皮书
     * @return 调用远程接口是否成功
     */
    this.publish = function (symbol, name, total, logo, introduce, whiteBook) {
        const data = {"symbol": symbol, "name": name, "total": total, "logo": logo, "introduce": introduce, "whiteBook": whiteBook};
        return axios.httpPostJson("/platform/publish", data);
    }

    /**
     * 转帐
     * @param contract 资产合约地址
     * @param from 付款地址
     * @param to 收款地址
     * @param amount 转帐数量
     * @param remark 备注
     * @return 调用远程接口是否成功
     */
    this.transfer = function (contract, from, to, amount, remark) {
        const data = {"contract": contract, "from": from, "to": to, "amount": amount, "remark": remark};
        return axios.httpPostJson("/platform/asset/transfer", data);
    }

    /**
     * 操作指定钱包资产数量
     * @param wallet 指定的钱包地址
     * @param contract 指定的合约地址
     * @param action 0: 解除冻结, 1: 冻结
     * @param amount 解除冻结或者冻结数量
     * @return 调用远程接口是否成功
     */
    this.updateWalletAssetStatus = function (wallet, contract, action, amount) {
        const data = {"wallet": wallet, "contract": contract, "action": action, "amount": amount};
        return axios.httpPostJson("/platform/asset/updateUserAssetStatus", data);
    }

    /**
     * 注册平台用户
     * @param uid 该平台用户唯一标识，不能重复
     * @return 该用户钱包地址
     */
    this.registerUser = function (uid) {
        const data = {"uid": uid};
        return axios.httpPostJson("/platform/registerUser", data);
    }

    /**
     * 获取平台信息
     * @return 平台信息封装对象
     */
    this.getPlatformInfo = function () {
        return axios.httpGet("/platform/info");
    }

    /**
     * 获取平台资产列表
     * @return 平台资产列表
     */
    this.getPlatformAssets = function () {
        return axios.httpGet("/platform/asset/listPlatformAssets");
    }

    /**
     * 获取指定钱包持有资产列表
     * @param address 指定的钱包地址
     * @return 资产列表
     */
    this.getWalletAssets = function (address) {
        const data = {"address": address};
        return axios.httpGet("/platform/asset/listUserAssets", data);
    }

    /**
     * 获取指定钱包指定资产持有信息
     * @param address 指定的钱包地址
     * @param contract 指定的合约地址
     * @return 资产信息
     */
    this.getWalletAsset = function (address, contract) {
        const data = {"address": address, "contract": contract};
        return axios.httpGet("/platform/asset/userAsset", data);
    }

    /**
     * 获取指定钱包资产变动记录
     * @param address 要获取的钱包地址
     * @param contract 合约地址, 不传则是平台下的所有资产变动
     * @param page 页码，1开始
     * @param limit 每页数量
     * @return 资产变动列表
     */
    this.getWalletAssetLog = function (address, contract, page, limit) {
        const data = {"address": address, "contract": contract, "page": page, "limit": limit};
        return axios.httpGet("/platform/asset/userAssetLog", data);
    }

    /**
     * 获取资产详情
     * @param contract 指定的合约地址
     * @return 资产详情
     */
    this.getAssetInfo = function (contract) {
        const data = {"contract": contract};
        return axios.httpGet("/platform/asset/info", data);
    }
}

const WalletSDK = function (no, accessKey, secretKey) {
    this.no = no;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.api = new Api(this, new AxiosEngine(this));
}