package wallet

import (
	"aesecb"
	"crypto/tls"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"
)

type Api struct {
	no        *string
	accessKey *string
	secretKey *string
	token     *string
	baseUrl   string
	client    *http.Client
}

type result struct {
	Code    int         `json:"code"`
	Data    interface{} `json:"data"`
	Message string      `json:"message"`
}

func (result *result) isSuccessful() bool {
	return result.Code == 200
}

type token struct {
	Token          string    `json:"token"`
	Expires        int64     `json:"expires"`
	ExpireDateTime time.Time `json:"expire_date_time"`
}

type Platform struct {
	No           string      `json:"no,omitempty"`
	AccessKey    string      `json:"accessKey,omitempty"`
	SecretKey    string      `json:"secretKey,omitempty"`
	Name         string      `json:"name,omitempty"`
	Wallet       string      `json:"wallet,omitempty"`
	Status       int         `json:"status,omitempty"`
	TransferRate json.Number `json:"transferRate,omitempty"`
	UpdateTime   string      `json:"updateTime,omitempty"`
	CreateTime   string      `json:"createTime,omitempty"`
}

type Asset struct {
	Symbol string `json:"symbol,omitempty"`
	//@SerializedName("contract", alternate = ["contractAddress"])
	Contract     string      `json:"contract,contractAddress,omitempty"`
	Name         string      `json:"name,omitempty"`
	Logo         string      `json:"logo,omitempty"`
	Number       json.Number `json:"number,omitempty"`
	Total        json.Number `json:"total,omitempty"`
	FreezeNumber json.Number `json:"freezeNumber,omitempty"`
	Introduce    string      `json:"introduce,omitempty"`
	WhiteBook    string      `json:"whiteBook,omitempty"`
	Status       int         `json:"status,omitempty"`
	UpdateTime   string      `json:"updateTime,omitempty"`
	CreateTime   string      `json:"createTime,omitempty"`

	From   string `json:"from,omitempty"`
	To     string `json:"to,omitempty"`
	Hash   string `json:"hash,omitempty"`
	Remark string `json:"remark,omitempty"`
}

func CreateSDK(no string, accessKey string, secretKey string) *Api {
	api := Api{
		client: &http.Client{
			Transport: &http.Transport{
				TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
			},
		},
		baseUrl:   "https://wallet.codbtoken.com/api",
		no:        &no,
		accessKey: &accessKey,
		secretKey: &secretKey,
	}
	return &api
}

func (api *Api) postJson(_url string, body interface{}, retry bool) []byte {
	data, ok := body.(string)
	var reader io.Reader
	if ok {
		if strings.HasSuffix(_url, "platform/token") {
			reader = strings.NewReader(data)
		} else {
			reader = strings.NewReader(bodyAesECBEncrypt(*api.secretKey, data))
		}
	} else {
		buffer, _ := json.Marshal(body)
		if strings.HasSuffix(_url, "platform/token") {
			reader = strings.NewReader(string(buffer))
		} else {
			reader = strings.NewReader(bodyAesECBEncrypt(*api.secretKey, string(buffer)))
		}
	}
	req, _ := http.NewRequest("POST", _url, reader)
	req.Header.Add("Content-Type", "application/json")
	if api.token != nil {
		req.Header.Add("token", *api.token)
	}
	response, err := api.client.Do(req)
	if err != nil {
		fmt.Printf("request faile: %v", err)
	} else {
		if response.StatusCode == 200 {
			buffer, _ := ioutil.ReadAll(response.Body)
			return buffer
		} else if response.StatusCode == 401 {
			result := api.getToken()
			if result != nil {
				token, ok := result.Data.(*token)
				if ok {
					api.token = &token.Token
					if retry {
						return api.postJson(_url, body, false)
					}
				}
			}
		}
	}
	return nil
}

func (api *Api) get(_url string, query map[string]string, retry bool) []byte {
	url_, err := url.Parse(_url)
	if url_ == nil {
		return nil
	}
	if query != nil {
		for key := range query {
			url_.Query().Add(key, query[key])
		}
	}
	req, _ := http.NewRequest("GET", url_.String(), nil)
	if api.token != nil {
		req.Header.Add("token", *api.token)
	}
	response, err := api.client.Do(req)
	if err != nil {
		fmt.Printf("request faile: %v", err)
	} else {
		if response.StatusCode == 200 {
			buffer, _ := ioutil.ReadAll(response.Body)
			return buffer
		} else if response.StatusCode == 401 {
			result := api.getToken()
			if result != nil {
				token, ok := result.Data.(*token)
				if ok {
					api.token = &token.Token
					if retry {
						return api.get(_url, query, false)
					}
				}
			}
		}
	}
	return nil
}

func (api *Api) url(path string) string {
	return fmt.Sprintf("%s%s", api.baseUrl, path)
}

func (api *Api) unmarshal(data []byte, typeStruct interface{}) bool {
	var result = &result{}
	_ = json.Unmarshal(data, result)
	encBuffer := result.Data.(string)
	if result.isSuccessful() && len(encBuffer) > 0 {
		decBuffer := bodyAesECBDecrypt(*api.secretKey, encBuffer)
		err := json.Unmarshal(decBuffer, typeStruct)
		if err == nil {
			return true
		}
	}
	return false
}

func (api *Api) getToken() *result {
	var body = map[string]string{
		"no":        *api.no,
		"accessKey": *api.accessKey,
	}
	data := api.postJson(api.url("/platform/token"), body, true)
	if data != nil {
		var result = &result{
			Data: &token{},
		}
		_ = json.Unmarshal(data, result)
		return result
	}
	return nil
}

func (api *Api) Publish(symbol string, name string, total string, logo string, introduce string, whiteBook string) bool {
	data := api.postJson(api.url("/platform/publish"), map[string]string{
		"symbol":    symbol,
		"name":      name,
		"total":     total,
		"logo":      logo,
		"introduce": introduce,
		"whiteBook": whiteBook,
	}, true)
	if data != nil {
		var result = &result{}
		_ = json.Unmarshal(data, result)
		if result.isSuccessful() {
			return true
		}
	}
	return false
}

func (api *Api) Transfer(contract string, from string, to string, amount string, remark string) bool {
	data := api.postJson(api.url("/platform/asset/transfer"), map[string]string{
		"contract": contract,
		"from":     from,
		"to":       to,
		"amount":   amount,
		"remark":   remark,
	}, true)
	if data != nil {
		var result = &result{}
		_ = json.Unmarshal(data, result)
		if result.isSuccessful() {
			return true
		}
	}
	return false
}

func (api *Api) RegisterUser(uid string) *string {
	data := api.postJson(api.url("/platform/registerUser"), map[string]string{"uid": uid}, true)
	if data != nil {
		var result = &result{}
		_ = json.Unmarshal(data, result)
		str, ok := result.Data.(string)
		if ok {
			buffer := bodyAesECBDecrypt(*api.secretKey, string(str))
			if buffer != nil {
				var address = string(buffer)
				return &address
			}
		}
	}
	return nil
}

func (api *Api) UpdateWalletAssetStatus(wallet string, contract string, action int, amount string) bool {
	data := api.postJson(api.url("/platform/asset/updateUserAssetStatus"), map[string]interface{}{
		"contract": contract,
		"wallet":   wallet,
		"action":   action,
		"amount":   amount,
	}, true)
	if data != nil {
		var result = &result{}
		_ = json.Unmarshal(data, result)
		if result.isSuccessful() {
			return true
		}
	}
	return false
}

func (api *Api) GetPlatformInfo() *Platform {
	data := api.get(api.url("/platform/info"), nil, true)
	if data != nil {
		var platform = &Platform{}
		if api.unmarshal(data, platform) {
			return platform
		}
	}
	return nil
}

func (api *Api) GetPlatformAssets() []*Asset {
	data := api.get(api.url("/platform/asset/listPlatformAssets"), nil, true)
	if data != nil {
		println(string(data))
		var assets []*Asset
		if api.unmarshal(data, &assets) {
			return assets
		}
	}
	return nil
}

func (api *Api) GetWalletAssetLog(address string, contract string, page int, limit int) []*Asset {
	data := api.get(api.url("/platform/asset/userAssetLog"), map[string]string{
		"address":  address,
		"contract": contract,
		"page":     strconv.Itoa(page),
		"limit":    strconv.Itoa(limit),
	}, true)
	if data != nil {
		var assets []*Asset
		if api.unmarshal(data, assets) {
			return assets
		}
	}
	return nil
}

func (api *Api) GetWalletAssets(address string) []*Asset {
	data := api.get(api.url("/platform/asset/listUserAssets"), map[string]string{"address": address}, true)
	if data != nil {
		var assets []*Asset
		if api.unmarshal(data, assets) {
			return assets
		}
	}
	return nil
}

func (api *Api) GetWalletAsset(address string, contract string) *Asset {
	data := api.get(api.url("/platform/asset/userAsset"), map[string]string{"address": address, "contract": contract}, true)
	if data != nil {
		var asset = &Asset{}
		if api.unmarshal(data, asset) {
			return asset
		}
	}
	return nil
}

func (api *Api) GetAssetInfo(contract string) *Asset {
	data := api.get(api.url("/platform/asset/info"), map[string]string{"contract": contract}, true)
	if data != nil {
		var asset = &Asset{}
		if api.unmarshal(data, asset) {
			return asset
		}
	}
	return nil
}

func bodyAesECBDecrypt(key string, base64Text string) []byte {
	err := aesecb.SetAesKey(key)
	if err != nil {
		fmt.Printf("aes key初始化失败 %s\n", err)
	}
	sed, err := base64.StdEncoding.DecodeString(base64Text)
	if err != nil {
		fmt.Printf("base64 解码失败 %s\n", err)
	}
	sd, err := aesecb.AesECBDecrypt(sed)
	if err != nil {
		fmt.Printf("aes 解密失败 %s\n", err)
	}
	return sd
}

func bodyAesECBEncrypt(key string, date string) string {
	err := aesecb.SetAesKey(key)
	if err != nil {
		fmt.Printf("aes key初始化失败 %s\n", err)
	}
	se, err := aesecb.AesECBEncrypt([]byte(date))
	if err != nil {
		fmt.Printf("aes 加密失败 %s\n", err)
	}
	s := base64.StdEncoding.EncodeToString(se)
	return s
}
