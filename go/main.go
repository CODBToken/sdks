package main

import(
	"C"
	"wallet"
)

var api *wallet.Api

//export Api
func Api() *wallet.Api{
	if api == nil{
		println("create")
		api = wallet.CreateSDK("21444697862578557", "UFvrZDy2u9EBOcY43aHD1vE6v7ABXw4H", "m8V9n9GLzkh3ZKRr")
	}
	return api
}

func main() {
	Api().GetPlatformAssets()
	Api().GetPlatformAssets()
	Api().GetPlatformAssets()
}
