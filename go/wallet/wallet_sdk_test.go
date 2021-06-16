package wallet

import (
	"fmt"
	"github.com/shopspring/decimal"
	"testing"
)

func TestRequest(t *testing.T) {
	api := CreateSDK("", "", "")
	println(api.GetPlatformUserInfo("oapfJ5aJY_iWpl_mo3ZP0eH3-XSI")["wallet"])
	println("--------------------")

	//https://github.com/shopspring/decimal
	price, err := decimal.NewFromString("136.02")
	if err != nil {
		panic(err)
	}
	fmt.Println("price:", price)
}
