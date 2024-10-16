// THIS FILE WAS GENERATED BY `xtp-go-bindgen`. DO NOT EDIT.
package main

import (
	"time"

	pdk "github.com/extism/go-pdk"
)

//export transform
func _Transform() int32 {
	var err error
	_ = err
	pdk.Log(pdk.LogDebug, "Transform: getting JSON input")
	var input Record
	err = pdk.InputJSON(&input)
	if err != nil {
		pdk.SetError(err)
		return -1
	}

	pdk.Log(pdk.LogDebug, "Transform: calling implementation function")
	output, err := Transform(input)
	if err != nil {
		pdk.SetError(err)
		return -1
	}

	pdk.Log(pdk.LogDebug, "Transform: setting JSON output")
	err = pdk.OutputJSON(output)
	if err != nil {
		pdk.SetError(err)
		return -1
	}

	pdk.Log(pdk.LogDebug, "Transform: returning")
	return 0
}

// A key/value header pair.
type Header struct {
	Key   string `json:"key"`
	Value string `json:"value"`
}

// An order from the market.
type Order struct {
	// Date/time of the order
	Date time.Time `json:"date"`
	// Closing price of the order
	Price float64 `json:"price"`
	// The volume of the order
	Volume int64 `json:"volume"`
}

// A plain key/value record.
type Record struct {
	Headers []Header `json:"headers"`
	Key     string   `json:"key"`
	Topic   string   `json:"topic"`
	// An order from the market.
	Value Order `json:"value"`
}

// Note: leave this in place, as the Go compiler will find the `export` function as the entrypoint.
func main() {}