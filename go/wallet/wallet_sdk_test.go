package wallet

import (
	"fmt"
	"testing"
	"github.com/shopspring/decimal"
)

func TestRequest(t *testing.T) {
	api := CreateSDK("21444697862578557", "UFvrZDy2u9EBOcY43aHD1vE6v7ABXw4H", "m8V9n9GLzkh3ZKRr")
	println(*api.RegisterUser("dssddddddsdd"))
	println("--------------------")

	//https://github.com/shopspring/decimal
	price, err := decimal.NewFromString("136.02")
	if err != nil {
		panic(err)
	}
	fmt.Println("price:", price)
}
