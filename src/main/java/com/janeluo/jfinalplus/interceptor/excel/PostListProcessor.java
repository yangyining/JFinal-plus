package com.janeluo.jfinalplus.interceptor.excel;

import java.util.List;

public interface PostListProcessor<T>{
	void process(List<T> list) ;
}
