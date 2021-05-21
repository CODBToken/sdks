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
    this.publish = function (symbol, name, total, logo, introduce, whiteBook) {
        const data = {"symbol": symbol, "name": name, "total": total, "logo": logo, "introduce": introduce, "whiteBook": whiteBook};
        return axios.httpPostJson("/platform/publish", data);
    }
    this.transfer = function (contract, from, to, amount, remark) {
        const data = {"contract": contract, "from": from, "to": to, "amount": amount, "remark": remark};
        return axios.httpPostJson("/platform/asset/transfer", data);
    }
    this.updateWalletAssetStatus = function (wallet, contract, action, amount) {
        const data = {"wallet": wallet, "contract": contract, "action": action, "amount": amount};
        return axios.httpPostJson("/platform/asset/updateUserAssetStatus", data);
    }
    this.registerUser = function (uid) {
        const data = {"uid": uid};
        return axios.httpPostJson("/platform/registerUser", data);
    }
    this.getPlatformInfo = function () {
        return axios.httpGet("/platform/info");
    }
    this.getPlatformAssets = function () {
        return axios.httpGet("/platform/asset/listPlatformAssets");
    }
    this.getWalletAssets = function (address) {
        const data = {"address": address};
        return axios.httpGet("/platform/asset/listUserAssets", data);
    }
    this.getWalletAsset = function (address, contract) {
        const data = {"address": address, "contract": contract};
        return axios.httpGet("/platform/asset/userAsset", data);
    }
    this.getWalletAssetLog = function (address, contract, page, limit) {
        const data = {"address": address, "contract": contract, "page": page, "limit": limit};
        return axios.httpGet("/platform/asset/userAssetLog", data);
    }
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