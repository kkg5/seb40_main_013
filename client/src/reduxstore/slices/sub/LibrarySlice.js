import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import Apis from "../../../apis/apis";

export const getLibrary = createAsyncThunk(
  "getLibrary", 
  async ({ mainCateClick, page, sortArgument, third }) => {
    console.log(222, { mainCateClick, subclick, page, sortArgument });
  return Apis.get(`products?main=${mainCateClick}&page=${page}&sortType=${sortArgument}&order=${third}`)
    .then((res) => {
      console.log(`shopslice`, res.data);
      return res.data;
    })
    .catch((err) => {
      console.log(err);
    });
});

export const getSub = createAsyncThunk(
  "getSub",
  async ({ mainCateClick, subclick, page, sortArgument, third }) => {
    return Apis.get(
      `products?main=${mainCateClick}&sub=${subclick}&page=${page}&sortType=${sortArgument}&order=${third}`
    )
      .then((res) => {
        console.log(res.data);
        return res.data;
      })
      .catch((err) => {
        console.log(err);
      });
  }
);

export const getAsc = createAsyncThunk(
  "getAsc",
  async ({ subclick, page, sortArgument, third }) => {
    return Apis.get(
      `products?main=서재&sub=${subclick}&page=${page}&sortType=${sortArgument}&order=${third}`
    )
      .then((res) => {
        console.log(`shopslice`, res.data);
        return res.data;
      })
      .catch((err) => {
        console.log(err);
      });
  }
);

export const getCount = createAsyncThunk("getCount", async () => {
  return Apis.get(`products/count?main=서재`)
    .then((res) => {
      console.log(`shopslice`, res.data);
      return res.data;
    })
    .catch((err) => {
      console.log(err);
    });
});

const librarySlice = createSlice({
  name: "library",
  initialState: {
    libraryInitial: [],
    subInitial: [],
    ascInitial: [],
    coutnInitial: {},
    loading: false,
    error: "",
  },
  reducers: {},
  extraReducers: {
    [getLibrary.fulfilled]: (state, action) => {
      state.libraryInitial = action.payload.content;
      state.loading = true;
      state.error = "";
    },
    [getSub.fulfilled]: (state, action) => {
      state.libraryInitial =  [...state.libraryInitial].concat(action.payload.content)
      state.loading = true;
      state.error = "";
    },

    [getAsc.fulfilled]: (state, action) => {
      state.ascInitial = action.payload.content;
      state.loading = true;
      state.error = "";
    },
    [getCount.fulfilled]: (state, action) => {
      state.coutnInitial = action.payload;
      state.loading = true;
      state.error = "";
    },
  },
});

export default librarySlice.reducer;
