package com.jwzt.caibian.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jwzt.caibian.activity.ChuanLianShenheActivity;
import com.jwzt.caibian.view.PullToRefreshLayout;
import com.jwzt.cb.product.R;
import com.jwzt.caibian.activity.ChuanLianGaodanYiShenActivity;
import com.jwzt.caibian.adapter.ChuanlianListAdapter;
import com.jwzt.caibian.application.BaseFragment;
import com.jwzt.caibian.application.CbApplication;
import com.jwzt.caibian.application.Configs;
import com.jwzt.caibian.bean.ChuanlIanListBean;
import com.jwzt.caibian.bean.LoginBean;
import com.jwzt.caibian.bean.TestGroupBean;
import com.jwzt.caibian.util.BitmapUtils;
import com.jwzt.caibian.util.IsNonEmptyUtils;
import com.jwzt.caibian.util.TimeUtil;
import com.jwzt.caibian.widget.DragSortListView;
import com.jwzt.caibian.widget.MyListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 串联单待审状态列表
 * 
 * @author howie
 * 
 */
public class ChuanLianDanYiShenFragment extends BaseFragment {
	
	private CbApplication app;
	private LoginBean loginBean;
    private String userId;
    private MyListView listview;
    private List<ChuanlIanListBean> mList;
    private List<ChuanlIanListBean> mListMore;
    private ChuanlianListAdapter messageAdapter;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private PullToRefreshLayout prl_refersh;
    private int pageSize=10;
	private int pageTag=0;//表示是刷新状态还是加载更多状态0表示刷新，1表示加载更多
	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case 1:
				initView();
				break;
			case 2:
				int size=mList.size();
				mList.addAll(mListMore);
				messageAdapter.setList(mList);
				messageAdapter.notifyDataSetChanged();
				RadioGroup rg_nav_content = ((ChuanLianShenheActivity) getActivity()).rg_nav_content;
				RadioButton childAt = (RadioButton) rg_nav_content.getChildAt(1);
				childAt.setText("已审"+"("+size+")");
				System.out.println("已申======="+size);
				break;
			default:
				break;
			}

		};

	};
	private ImageView rl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		View view = View.inflate(getActivity(), R.layout.daishenlist_layout,null);
		app=(CbApplication) getActivity().getApplication();
		loginBean=app.getmLoginBean();
		userId=loginBean.getUserID();
		listview=(MyListView) view.findViewById(R.id.mlv);
		mList=new ArrayList<ChuanlIanListBean>();
		mListMore=new ArrayList<ChuanlIanListBean>();
		options = new DisplayImageOptions.Builder()  
	        .showImageOnLoading(R.drawable.replace) // 设置图片下载期间显示的图片  
	        .showImageForEmptyUri(R.drawable.replace) // 设置图片Uri为空或是错误的时候显示的图片  
	        .showImageOnFail(R.drawable.replace) // 设置图片加载或解码过程中发生错误显示的图片  
	        .cacheInMemory(false) // 设置下载的图片是否缓存在内存中  
	        .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中 
	        .bitmapConfig(Config.RGB_565)
         //.displayer(new FadeInBitmapDisplayer(100))
	        .build(); // 构建完成  
		  imageLoader = ImageLoader.getInstance();
		
		  //listview.setDropListener(onDrop);
		 
		  //listview.setRemoveListener(onRemove);
		  listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					   Intent  intent=new Intent(getActivity(),ChuanLianGaodanYiShenActivity.class);
					   intent.putExtra("id", mList.get(position).getId());
					intent.putExtra("ChuanlIanListBean",mList.get(position));
					   startActivity(intent);
					/*Intent intent=new Intent(getActivity(),PreviewUploadedActivity.class);
					intent.putExtra("id", mList.get(position).getId());
					String status=mList.get(position).getStatus();
					intent.putExtra("operateType", mList.get(position).getStatus());
					getActivity().startActivity(intent);*/
					
					/*Intent istent=new Intent(getActivity(),ShowNewsDeatilsActivity.class);
					istent.putExtra("ids", mList.get(position).getId());
					istent.putExtra("title",  mList.get(position).getTitle());
					istent.putExtra("createtime", mList.get(position).getCreateTime());
					istent.putExtra("contents", mList.get(position).getContent());
					startActivity(istent);*/
				}
			});
		  prl_refersh=(PullToRefreshLayout)view.findViewById(R.id.prl_refersh);
		prl_refersh.setOnRefreshListener(new MyListener());
		rl = view.findViewById(R.id.rl);
		//获取未审核数据
	   // initdata();
	   
		return view;
	}
	  private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
			@Override
			public void drop(int from, int to) {
				 ChuanlIanListBean  item = (ChuanlIanListBean )messageAdapter.getItem(from);//得到listview的适配器
				 messageAdapter.remove(from);//在适配器中”原位置“的数据。
				 messageAdapter.insert(item, to);//在目标位置中插入被拖动的控件。
				// listview.moveCheckState(from,to);
				//mList.set(to, mList.get(from));
				messageAdapter.notifyDataSetChanged();
				System.out.println("mlist"+mList);
			}
		};

		private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
			@Override
			public void remove(int which) {
				//videoFootageAdapter.remove(videoFootageAdapter.getItem(which));
				
			}
		};
	class MyListener implements PullToRefreshLayout.OnRefreshListener {
		@SuppressLint("HandlerLeak")
		@Override
		public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
			initdata();
			// 下拉刷新操作
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// 千万别忘了告诉控件刷新完毕了哦！
					pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
				}
			}.sendEmptyMessageDelayed(0, 2000);
		}
		@SuppressLint("HandlerLeak")
		@Override
		public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
			initDataMore();
			// 加载操作
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// 千万别忘了告诉控件加载完毕了哦！
					pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
				}
			}.sendEmptyMessageDelayed(0, 2000);//2秒之后触发viewPager的自动播放
		}
	}
	
	/**
	 * 加载更多
	 */
	private void initDataMore(){
		if(loginBean!=null){
			pageTag=1;
			if(IsNonEmptyUtils.isList(mList)){
				String liveListUrl=String.format(Configs.liveBackListUrl, loginBean.getUserID(),mList.get(mList.size()-1).getId(),pageSize);
				System.out.println("liveListUrl"+liveListUrl);
				RequestData(liveListUrl, Configs.liveBackListCode, 1);
			}
		}
	}
	private void initView(){
		if(mList.size()<=0){
			rl.setVisibility(View.VISIBLE);
		}else{
			rl.setVisibility(View.GONE);
		}
		messageAdapter = new ChuanlianListAdapter(getActivity(),mList);
		listview.setAdapter(messageAdapter);
		RadioGroup rg_nav_content = ((ChuanLianShenheActivity) getActivity()).rg_nav_content;
		RadioButton childAt = (RadioButton) rg_nav_content.getChildAt(1);
		childAt.setText("已审"+"("+mList.size()+")");
		//dismisLoadingDialog();
	}
	
	private void initdata() {

		pageTag=0;
		String liveListUrl=String.format(Configs.getChuanliandanyishen, loginBean.getUserID(),"",pageSize);
		System.out.println("liveListUrl"+liveListUrl);
		RequestData(liveListUrl, Configs.infoListCode, pageTag);
	
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 initdata();
	}

	@Override
	protected void initRequestOnSuccess(String result, int code, int biaoshi) {
		initDataParse(result,code);
		
	}

	@Override
	protected void initRequestOnStart(String result, int code, int biaoshi) {
		
		
	}

	@Override
	protected void initRequestOnFailure(String failure, int code, int biaoshi) {
		
		
	}

	@Override
	protected void initRequestOnCache(String result, int code, int biaoshi) {
		
		
	}

	private void initDataParse(String result, int code){
		if(code==Configs.infoListCode){
			if(pageTag==0){//刷新
				JSONObject jsonObject=JSON.parseObject(result);
				String status=jsonObject.getString("status");
				if(status.equals("100")){//表示获取成功
					String data=jsonObject.getString("data");
					mList=JSON.parseArray(data,ChuanlIanListBean.class);
					//遍历循环数据
				
					if(IsNonEmptyUtils.isList(mList)){
						mHandler.sendEmptyMessage(1);
					}else{
						mHandler.sendEmptyMessage(0);
					}
				}
			}else if(pageTag==1){//加载更多
				JSONObject jsonObject=JSON.parseObject(result);
				String status=jsonObject.getString("status");
				if(status.equals("100")){//表示获取成功
					String data=jsonObject.getString("data");
					mListMore=JSON.parseArray(data,ChuanlIanListBean.class);
					if(IsNonEmptyUtils.isList(mListMore)){
						mHandler.sendEmptyMessage(2);
					}else{
						mHandler.sendEmptyMessage(0);
					}
				}
			}
		}
	}
	
	public class MessageAdapter extends BaseAdapter {
		private Context mContext;
		private List<TestGroupBean> mList;
		
		public MessageAdapter(Context mContext,List<TestGroupBean> list) {
			super();
			this.mContext = mContext;
			this.mList=list;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup viewGroup) {
			ViewHolder holder = null;
			if(convertView==null){
				convertView = View.inflate(mContext, R.layout.daishen_item_layout,null);
				holder = new ViewHolder();
				holder.iv=(ImageView) convertView.findViewById(R.id.iv);
				holder.tv_title=(TextView) convertView.findViewById(R.id.tv_title);
				holder.tv_desc=(TextView) convertView.findViewById(R.id.tv_desc);
				holder.tv_time=(TextView) convertView.findViewById(R.id.tv_time);
				holder.tv_huifu=(TextView) convertView.findViewById(R.id.tv_huifu);
				holder.tv_shenhes=(TextView)convertView.findViewById(R.id.tv_shenhes);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
		     	holder.tv_title.setText(mList.get(position).getTitle());
				holder.iv.setImageBitmap(BitmapUtils.readBitMap(mContext, R.drawable.shen));
				holder.tv_desc.setText(mList.get(position).getCreateTime());
				holder.tv_shenhes.setVisibility(View.GONE);
				imageLoader.displayImage(mList.get(position).getNewsImage(), holder.iv, options);
				String status=mList.get(position).getStatus();
				if(status.endsWith("0")){
					holder.tv_huifu.setText("待提交");
				}else if(status.endsWith("1")){
					holder.tv_huifu.setText("已提交");
				}else if(status.endsWith("2")){
					holder.tv_huifu.setText("一审");
				}else if(status.endsWith("3")){
					holder.tv_huifu.setText("二审");
				}else if(status.endsWith("4")){
					holder.tv_huifu.setText("退回");
				}
				
				
	           /*holder.tv_huifu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				  //显示pipuwindow
					Intent intent=new Intent(getActivity(),FeedBackActivity.class);
					//intent.putExtra("userid", mLoginBean.getUserID());
					intent.putExtra("messageid", mList.get(position).getId());
				    getActivity().startActivity(intent);
				}
			});*/
		
			holder.tv_time.setText(TimeUtil.getDateDiff(mList.get(position).getCreateTime()));
			return convertView;
		}
		
		
		public class ViewHolder{
			/**条目左侧的圆形图片*/
			ImageView iv;
			/**标题信息和描述信息、审核通过的时间*/
			TextView tv_title,tv_desc,tv_time,tv_huifu,tv_shenhes;
		
		}
		public void setList(List<TestGroupBean> list2) {
			this.mList=list2;
			notifyDataSetChanged();
		}

	}

	
}