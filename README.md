# sdks

### PHP接入方式 [源码及注释](php/wallet_sdk.php)
1. `$api = WalletSDK::create("平台号", "访问密钥", "安全密钥");`
2. `$api->相关方法`

### Python接入方式 [源码及注释](python/wallet_sdk.py)
1. `api = WalletSDK("平台号", "访问密钥", "安全密钥").get_api()`
2. `api.相关方法`

### JavaScript接入方式 [源码及注释](js/walletsdk.js)
1. 依赖`axios`和`crypto-js`(在library文件夹中)
2. 引入`walletsdk.js`
3. `const sdk = new WalletSDK("平台号", "访问密钥", "安全密钥")`
4. `sdk.api.相关方法`

### golang接入方式 [源码及注释](go/wallet/wallet_sdk.go)
1. `api := CreateSDK("平台号", "访问密钥", "安全密钥")`
2. `*api.相关方法`

### Java接入方式 [源码及注释](java/src/main/kotlin/com/codb/sdk/Api.kt)
1. `api = WalletSDK.create("平台号", "访问密钥", "安全密钥")`
2. `api.相关方法`

## 警告!! 警告!! 警告!! 
### `WalletSDK`应在全局上下文存在一个api实例，请不要多次`create`创建，否则很可能被系统封IP
