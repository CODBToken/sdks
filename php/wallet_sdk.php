<?php

class ApiResponse
{
    private $code;
    private $data;
    private $message;

    private function __construct($code, $data, $message)
    {
        $this->code = $code;
        $this->data = $data;
        $this->message = $message;
        if ($message != null) {
            printf(" error: $message\r\n");
        } else {
            printf("\r\n");
        }
    }

    public static function create($code, $data, $message): ApiResponse
    {
        return new ApiResponse($code, $data, $message);
    }

    function code(): int
    {
        return $this->code;
    }

    function data()
    {
        return $this->data;
    }

    function getDecryptData(string $key)
    {
        $decrypt = openssl_decrypt(base64_decode($this->data, true), "aes-128-ecb", $key, OPENSSL_RAW_DATA);
        if ($decrypt) {
            $mixed = json_decode($decrypt);
            if ($mixed != null) {
                return $mixed;
            }
        }
        return $decrypt;
    }

    function isSuccessful(): bool
    {
        return $this->code == 200;
    }

    function error(): string
    {
        return $this->message;
    }
}

class WalletSDK
{
    protected $no;
    protected $accessKey;
    protected $secretKey;
    private $baseUrl = "https://wallet.codbtoken.com/api";

    private function __construct($no, $accessKey, $secretKey)
    {
        $this->no = $no;
        $this->accessKey = $accessKey;
        $this->secretKey = $secretKey;
    }

    public static function create($no, $accessKey, $secretKey): WalletSDK
    {
        return new WalletSDK($no, $accessKey, $secretKey);
    }

    private function sendRequest(string $url, $data, string $method, $contentType, int $timeout = 30, bool $proxy = false): ?ApiResponse
    {
        $ch = null;
        $ret = null;
        $header = array();
        $header[] = 'token: ' . $this->readToken();
        if ('POST' === strtoupper($method)) {
            $ch = curl_init($url);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_HEADER, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($ch, CURLOPT_FRESH_CONNECT, true);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_FORBID_REUSE, true);
            curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);
            if ($contentType != null) {
                $header[] = 'Content-Type: ' . $contentType;
            }
            curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
            if (is_string($data)) {
                curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
            } else {
                curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($data));
            }
        } else if ('GET' === strtoupper($method)) {
            if ($data != null) {
                if (is_string($data)) {
                    $real_url = $url . (strpos($url, '?') === false ? '?' : '') . $data;
                } else {
                    $real_url = $url . (strpos($url, '?') === false ? '?' : '') . http_build_query($data);
                }
            } else {
                $real_url = $url;
            }
            $ch = curl_init($real_url);
            curl_setopt($ch, CURLOPT_HEADER, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);
        } else {
            return ApiResponse::create(-1, null, "not support method: $method");
        }

        if ($proxy) {
            curl_setopt($ch, CURLOPT_PROXY, $proxy);
        }
        printf("$method: $url -> ");
        $ret = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        printf("http code: $httpCode");
        curl_close($ch);
        if ($ret && $httpCode == 200) {
//            printf("$ret\r\n");
            $obj = json_decode($ret);
            return ApiResponse::create($obj->code, $obj->data, $obj->message);
        } else {
            return ApiResponse::create($httpCode, null, "request fail");
        }
    }

    private function get(string $url, $data): ApiResponse
    {
        $response = $this->sendRequest($url, $data, "GET", null);
        if ($response->code() == 401) {
            $this->updateToken();
        } else {
            return $response;
        }
        return $this->sendRequest($url, $data, "GET", null);
    }

    private function postJson(string $url, $data, bool $encrypt = true): ApiResponse
    {
        if ($data != null) {
            if (!is_string($data)) {
                $data = json_encode($data);
                if (!$data) {
                    $data = null;
                } else if ($encrypt) {
                    $data = base64_encode(openssl_encrypt($data, "aes-128-ecb", $this->secretKey, OPENSSL_RAW_DATA));
                }
            }
        }
        $response = $this->sendRequest($url, $data, "POST", "application/json");
        if ($response->code() == 401) {
            $this->updateToken();
        } else {
            return $response;
        }
        return $this->sendRequest($url, $data, "POST", "application/json");
    }

    private function readToken(): string
    {
        $file = ".token";
        if (file_exists($file)) {
            $resource = fopen($file, "r+");
            if ($resource) {
                $token = fread($resource, filesize($file));
                fclose($resource);
                if ($token) {
                    return $token;
                }
            }
        }
        return "";
    }

    private function saveToken(string $token)
    {
        $file = ".token";
        $resource = fopen($file, "w+");
        if ($resource) {
            fwrite($resource, $token);
            fclose($resource);
        }
    }

    private function updateToken(): bool
    {
        $data = array("no" => $this->no, "accessKey" => $this->accessKey);
        $response = $this->postJson("$this->baseUrl/platform/token", $data, false);
        if ($response->isSuccessful()) {
            if (is_object($response->data())) {
                $this->saveToken($response->data()->token);
            }
        }
        return false;
    }

    /**
     * ????????????
     * @param string $symbol ?????????????????????????????????
     * @param string $name ??????????????????????????????0
     * @param string $total ????????????
     * @param string $logo ??????logo
     * @param string $introduce ????????????
     * @param string $whiteBook ?????????
     * @return bool ??????????????????????????????
     */
    function publish(string $symbol, string $name, string $total, string $logo, string $introduce, string $whiteBook): bool
    {
        $data = array(
            "symbol" => $symbol,
            "name" => $name,
            "total" => $total,
            "logo" => $logo,
            "introduce" => $introduce,
            "whiteBook" => $whiteBook
        );
        $response = $this->postJson("$this->baseUrl/platform/publish", $data);
        return $response->isSuccessful();
    }

    /**
     * ??????
     * @param string $contract ??????????????????
     * @param string $from ????????????
     * @param string $to ????????????
     * @param string $amount ????????????
     * @param string $remark ??????
     * @param string $gasContract ???????????????????????????
     * @param string $gasFee ??????
     * @return bool ??????????????????????????????
     */
    function transfer(string $contract, string $from, string $to, string $amount, string $remark, string $gasContract, string $gasFee): bool
    {
        $data = array(
            "contract" => $contract,
            "from" => $from,
            "to" => $to,
            "amount" => $amount,
            "remark" => $remark
        );
        if ($gasContract && $gasFee) {
            $data["gas"] = array(
                "contract" => $gasContract,
                "free" => $gasFee
            );;
        }
        $response = $this->postJson("$this->baseUrl/platform/asset/transfer", $data);
        return $response->isSuccessful();
    }

    /**
     * ??????????????????
     * @param string $uid ??????????????????????????????????????????
     * @return string|null ?????????????????????
     */
    function registerUser(string $uid): ?string
    {
        $response = $this->postJson("$this->baseUrl/platform/registerUser", array("uid" => $uid));
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            return (string)$data;
        }
        return null;
    }

    /**
     * ??????????????????????????????
     * @param string $wallet ?????????????????????
     * @param string $contract ?????????????????????
     * @param string $action 0: ????????????, 1: ??????
     * @param string $amount ??????????????????????????????
     * @return bool ??????????????????????????????
     */
    function updateWalletAssetStatus(string $wallet, string $contract, string $action, string $amount): bool
    {
        $data = array(
            "wallet" => $wallet,
            "contract" => $contract,
            "action" => $action,
            "amount" => $amount
        );
        $response = $this->postJson("$this->baseUrl/platform/asset/updateUserAssetStatus", $data);
        return $response->isSuccessful();
    }

    /**
     * ??????????????????
     * @return Platform|null ????????????????????????
     */
    function getPlatformInfo(): ?Platform
    {
        $response = $this->get("$this->baseUrl/platform/info", null);
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            if (is_object($data)) {
                return Platform::build($data);
            }
        }
        return null;
    }

    /**
     * ????????????????????????
     * @param string $uid ????????????ID
     * @return object ??????????????????
     */
    function getPlatformUserInfo(string $uid): ?object
    {
        $response = $this->get("$this->baseUrl/platform/platformUserInfo", array(
            "uid" => $uid,
        ));
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            if (is_object($data)) {
                return (object)$data;
            }
        }
        return null;
    }

    /**
     * ????????????????????????
     * @return array ??????????????????
     * if($asset instanceof Asset){
     *   printf($asset->symbol);
     * }
     */
    function getPlatformAssets(): array
    {
        $response = $this->get("$this->baseUrl/platform/asset/listPlatformAssets", null);
        $assets = array();
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            if (is_array($data)) {
                foreach ($data as $value) {
                    $assets[] = Asset::build($value);
                }
            }
        }
        return $assets;
    }

    /**
     * ????????????????????????????????????
     * @param string $address ????????????????????????
     * @param string $contract ????????????, ??????????????????????????????????????????
     * @param int $page ?????????1??????
     * @param int $limit ????????????
     * @return array ??????????????????
     */
    function getWalletAssetLog(string $address, string $contract, int $page, int $limit): array
    {
        $response = $this->get("$this->baseUrl/platform/asset/userAssetLog", array(
            "address" => $address,
            "contract" => $contract,
            "page" => $page,
            "limit" => $limit,
        ));
        $assets = array();
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            if (is_array($data)) {
                foreach ($data as $value) {
                    $assets[] = Asset::build($value);
                }
            }
        }
        return $assets;
    }

    /**
     * ????????????????????????????????????
     * @param string $address ?????????????????????
     * @return array ????????????
     */
    function getWalletAssets(string $address): array
    {
        $response = $this->get("$this->baseUrl/platform/asset/listUserAssets", array("address" => $address));
        $assets = array();
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            if (is_array($data)) {
                foreach ($data as $value) {
                    $assets[] = Asset::build($value);
                }
            }
        }
        return $assets;
    }

    /**
     * ??????????????????????????????????????????
     * @param string $address ?????????????????????
     * @param string $contract ?????????????????????
     * @return Asset|null ????????????
     */
    function getWalletAsset(string $address, string $contract): ?Asset
    {
        $response = $this->get("$this->baseUrl/platform/asset/userAsset", array(
            "address" => $address,
            "contract" => $contract
        ));
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            return Asset::build($data);
        }
        return null;
    }

    /**
     * ??????????????????
     * @param string $contract ?????????????????????
     * @return Asset|null ????????????
     */
    function getAssetInfo(string $contract): ?Asset
    {
        $response = $this->get("$this->baseUrl/platform/asset/info", array("contract" => $contract));
        if ($response->isSuccessful()) {
            $data = $response->getDecryptData($this->secretKey);
            return Asset::build($data);
        }
        return null;
    }
}

class Platform
{
    var $no;
    var $accessKey;
    var $secretKey;
    var $name;
    var $wallet;
    var $status;
    var $transferRate;
    var $updateTime;
    var $createTime;

    public static function build($data): Platform
    {
        $platform = new Platform();
        $platform->no = $data->symbol;
        $platform->accessKey = $data->accessKey;
        $platform->secretKey = $data->secretKey;
        $platform->name = $data->name;
        $platform->wallet = $data->wallet;
        $platform->status = $data->status;
        $platform->transferRate = $data->transferRate;
        $platform->updateTime = $data->updateTime;
        $platform->createTime = $data->createTime;
        return $platform;
    }
}

class Asset
{
    var $symbol;
    var $contract;
    var $name;
    var $logo;
    var $number;
    var $total;
    var $freezeNumber; //???????????????
    var $useFreezeNumber; //????????????
    var $introduce;
    var $whiteBook;
    var $status;
    var $updateTime;
    var $createTime;

    var $from;
    var $to;
    var $hash;
    var $remark;

    public static function build($data): Asset
    {
        $asset = new Asset();
        $asset->symbol = $data->symbol;
        $asset->contract = $data->contract;
        $asset->name = $data->name;
        $asset->logo = $data->logo;
        $asset->number = $data->number;
        $asset->total = $data->total;
        $asset->freezeNumber = $data->freezeNumber;
        $asset->useFreezeNumber = $data->useFreezeNumber;
        $asset->introduce = $data->introduce;
        $asset->whiteBook = $data->whiteBook;
        $asset->status = $data->status;
        $asset->updateTime = $data->updateTime;
        $asset->from = $data->from;
        $asset->to = $data->to;
        $asset->hash = $data->hash;
        $asset->remark = $data->remark;
        return $asset;
    }
}

//$api = WalletSDK::create("", "", "");
//print($api->getPlatformUserInfo("oapfJ5aJY_iWpl_mo3ZP0eH3-XSI")->wallet);
//$assets = $api->transfer("0x4cb89ac30f2342c89ca4025ba474083f4f205ca0", "0x3e754484ea14f521b02738c2aede37b527c4283d", "2", "3", "", "xx", "xxx");
//foreach ($assets as $asset) {
//    if ($asset instanceof Asset) {
//        printf("$asset->number \r\n");
//    }
//}