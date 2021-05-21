module main

go 1.23

require (
	aesecb v0.0.0
	wallet v0.0.0
)
replace aesecb => ./wallet/aesecb
replace wallet => ./wallet