import base64
import json

import requests

try:
    from Crypto.Cipher import AES
    from Crypto.Util.Padding import pad, unpad
except ImportError:
    print('请安装加解密库pycryptodome')


class Http:
    def __init__(self, sdk):
        self.__sdk = sdk
        self.__aes = AES.new(key=sdk.secret_key.encode("utf-8"), mode=AES.MODE_ECB)
        self.__unpad = lambda date: date[0:-ord(date[-1])]
        self.__token = ""

    def get(self, path, params=None):
        headers = {
            "token": self.__token
        }
        response = requests.get(url=self.__sdk.base_url + path, params=params, headers=headers)
        if response.status_code == 401 and self.__update_token():
            return self.get(path, params)
        response = json.loads(response.content, strict=False)
        if path != "/platform/token" and response["code"] == 200:
            data = response["data"]
            data = self.__decrypt(data)
            response["data"] = json.loads(data, strict=False)
        return response

    def post_json(self, path, json_data):
        headers = {
            "token": self.__token,
            "Content-Type": "application/json"
        }
        body = json_data
        if path != "/platform/token" and body:
            body = self.__encrypt(json.dumps(body))
        else:
            body = json.dumps(body)
        response = requests.post(url=self.__sdk.base_url + path, data=body, headers=headers)
        if response.status_code == 401 and self.__update_token():
            return self.post_json(path, json_data)
        response = json.loads(bytes.decode(response.content, "utf-8"))
        if path != "/platform/token" and response["code"] == 200:
            data = self.__decrypt(response["data"])
            if str.startswith(data, "[") or str.startswith(data, "{"):
                response["data"] = json.loads(data, strict=False)
            else:
                response["data"] = data
        return response

    def __pad__(self, text):
        count = len(text.encode('utf-8'))
        add = AES.block_size - (count % AES.block_size)
        entext = text + (chr(add) * add)
        return entext

    def __decrypt(self, data):
        data = self.__aes.decrypt(base64.decodebytes(data.encode("utf-8"))).decode("utf-8")
        return self.__unpad(data)

    def __encrypt(self, data):
        data = self.__aes.encrypt(self.__pad__(data).encode("utf8"))
        data = bytes.decode(base64.b64encode(data))
        return data

    def __update_token(self):
        data = {"no": self.__sdk.no, "accessKey": self.__sdk.access_key}
        data = self.post_json("/platform/token", data)
        if data["code"] == 200:
            new_token = data["data"]["token"]
            self.__token = new_token
            return True
        return False


class WalletSDK:
    def __init__(self, no, access_key, secret_key):
        self.base_url = "https://wallet.codbtoken.com/api"
        self.no = no
        self.access_key = access_key
        self.secret_key = secret_key
        self.__api = Api(self)

    def get_api(self):
        return self.__api


class Api:
    def __init__(self, sdk):
        self.__http = Http(sdk)

    def publish(self, symbol, name, total, logo, introduce, white_book):
        data = {"symbol": symbol, "name": name, "total": total, "logo": logo, "introduce": introduce, "whiteBook": white_book}
        return self.__http.post_json("/platform/publish", data)

    def transfer(self, contract, _from, to, amount, remark):
        data = {"contract": contract, "from": _from, "to": to, "amount": amount, "remark": remark}
        return self.__http.post_json("/platform/asset/transfer", data)

    def update_wallet_asset_status(self, wallet, contract, action, amount):
        data = {"wallet": wallet, "contract": contract, "action": action, "amount": amount}
        return self.__http.post_json("/platform/asset/updateUserAssetStatus", data)

    def register_user(self, uid):
        data = {"uid": uid}
        return self.__http.post_json("/platform/registerUser", data)

    def get_platform_assets(self):
        return self.__http.get("/platform/asset/listPlatformAssets")

    def get_platform_info(self):
        return self.__http.get("/platform/info")

    def get_wallet_assets(self, address):
        data = {"address": address}
        return self.__http.get("/platform/asset/listUserAssets", data)

    def get_wallet_asset(self, address, contract):
        data = {"address": address, "contract": contract}
        return self.__http.get("/platform/asset/userAsset", data)

    def get_wallet_asset_log(self, address, contract, page, limit):
        data = {"address": address, "contract": contract, "page": page, "limit": limit}
        return self.__http.get("/platform/asset/userAssetLog", data)

    def get_asset_info(self, contract):
        data = {"contract": contract}
        return self.__http.get("/platform/asset/info", data)


# if __name__ == "__main__":
#     api = WalletSDK("21444697862578557", "UFvrZDy2u9EBOcY43aHD1vE6v7ABXw4H", "m8V9n9GLzkh3ZKRr").get_api()
#     resp = api.get_platform_assets()
#     print(resp)
#     resp = api.get_wallet_asset_log("0x4cb89ac30f2342c89ca4025ba474083f4f205ca0", "0x3e754484ea14f521b02738c2aede37b527c4283d", 1, 3)
#     print(resp)
