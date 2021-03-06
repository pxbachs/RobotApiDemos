/*
 * Copyright (C) 2014 FPT Corporation
 * @author: Robot Team (FTI)
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fpt.robot.example.apis.motion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.fpt.robot.RobotException;
import com.fpt.robot.RobotHardware;
import com.fpt.robot.example.apis.R;
import com.fpt.robot.example.apis.RobotApiDemoActivity;
import com.fpt.robot.motion.RobotMotionCartesianController;
import com.fpt.robot.motion.RobotMotionSelfCollisionProtection;
import com.fpt.robot.motion.RobotMotionStiffnessController;
import com.fpt.robot.types.RobotPosition6D;
/**
 * This class is used to set position of a chain for robot
 * @author Robot Team (FTI)
 *
 */
public class MotionSetPosition extends RobotApiDemoActivity implements
		OnClickListener {
	private final String TAG = "MotionSetPosition";
	private final String INSTRUCTIONS = "This class is used to set position of a chain for robot";

	// button set position
	private Button btSetPosition;
	// button get robot position
	private Button btGetPosition;
	
	// text view display robot position
	private TextView tvPosition;

	// spinner name of chains
	private Spinner spChainNames;
	// spinner space
	private Spinner spSpace;
	// spinner axisMask
	private Spinner spAxisMask;

	// edit text to enter robot position 6D
	private EditText edX;
	private EditText edY;
	private EditText edZ;
	private EditText edWx;
	private EditText edWy;
	private EditText edWz;

	// chain names
	private String[] chainNames = new String[] { RobotHardware.Chain.HEAD,
			RobotHardware.Chain.LEFT_ARM, RobotHardware.Chain.LEFT_LEG,
			RobotHardware.Chain.RIGHT_ARM, RobotHardware.Chain.RIGHT_LEG,
			RobotHardware.Chain.TORSO, };
	// space values
	private String[] spaces = new String[] { "FRAME_ROBOT", "FRAME_WORLD",
			"FRAME_TORSO" };
	// axisMask values
	private String[] axisMask = new String[] { "7", "56", "63" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// show instructions
		showInfoDialog(TAG, INSTRUCTIONS);
		// set event click for buttons
		btSetPosition.setOnClickListener(this);
		btGetPosition.setOnClickListener(this);
		// set adapter chains name for spinner
		spChainNames.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, chainNames));
		spChainNames.setSelection(0);
		// set adapter space values for spinner
		spSpace.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, spaces));
		spSpace.setSelection(0);
		// set adapter axisMask values for spinner
		spAxisMask.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, axisMask));
		spAxisMask.setSelection(0);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void settingView() {
		btSetPosition = (Button) findViewById(R.id.btSetPosition);
		btGetPosition = (Button) findViewById(R.id.btGetPosition);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		spChainNames = (Spinner) findViewById(R.id.spChainName);
		spSpace = (Spinner) findViewById(R.id.spSpace);
		spAxisMask = (Spinner) findViewById(R.id.spAxisMask);
		edX = (EditText) findViewById(R.id.edSetPositionX);
		edY = (EditText) findViewById(R.id.edSetPositionY);
		edZ = (EditText) findViewById(R.id.edSetPositionZ);
		edWx = (EditText) findViewById(R.id.edSetPositionWx);
		edWy = (EditText) findViewById(R.id.edSetPositionWy);
		edWz = (EditText) findViewById(R.id.edSetPositionWz);

		super.settingView();
	}

	/**
	 * Set position for a chain name
	 */
	private void setPosition() {
		// get value robot position 6D from edit text
		final String x = edX.getText().toString();
		final String y = edY.getText().toString();
		final String z = edZ.getText().toString();
		final String wx = edWx.getText().toString();
		final String wy = edWy.getText().toString();
		final String wz = edWz.getText().toString();
		// validate data from edit text
		if (x == null || TextUtils.isEmpty(x) || y == null
				|| TextUtils.isEmpty(y) || z == null || TextUtils.isEmpty(z)
				|| wx == null || TextUtils.isEmpty(wx) || wy == null
				|| TextUtils.isEmpty(wy) || wz == null || TextUtils.isEmpty(wz)) {
			showInfoDialog("Motion Others",
					"Please set value for x,y,z,wz,wy,wz");
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				showProgress("Setting position...");
				// get chain name
				String name = (String) spChainNames.getSelectedItem();
				// get space
				String spaceString = (String) spSpace.getSelectedItem();
				int space = RobotMotionCartesianController.FRAME_ROBOT;
				if (spaceString.equalsIgnoreCase("FRAME_WORLD")) {
					space = RobotMotionCartesianController.FRAME_WORLD;
				} else if (spaceString.equalsIgnoreCase("FRAME_TORSO")) {
					space = RobotMotionCartesianController.FRAME_TORSO;
				}
				// get axixMask
				int axisMask = Integer.parseInt((String) spAxisMask
						.getSelectedItem());
				// create new object robot position 6D
				RobotPosition6D position = new RobotPosition6D(
						Float.parseFloat(x), Float.parseFloat(y),
						Float.parseFloat(z), Float.parseFloat(wx),
						Float.parseFloat(wy), Float.parseFloat(wz));
				float fractionMaxSpeed = 1.0f;
				try {
					if (name.equalsIgnoreCase("arm")
							|| name.equalsIgnoreCase("larm")
							|| name.equalsIgnoreCase("rarm")) {
						RobotMotionSelfCollisionProtection.setEnable(
								getRobot(), name, true);
					}
					// need set stiffness first
					RobotMotionStiffnessController.setStiffnesses(getRobot(),
							new String[] { name }, new float[] { 1.0f });
					// set position for a chain name
					RobotMotionCartesianController.setPosition(getRobot(),
							name, space, position, axisMask, fractionMaxSpeed);
					showInfoDialogFromThread("MotionOthers",
							"Set position successfully");
				} catch (RobotException e) {
					cancelProgress();
					showInfoDialogFromThread("MotionOthers",
							"Set position failed: " + e.getMessage());
					e.printStackTrace();
				}
				cancelProgress();
			}
		}).start();
	}

	/**
	 * Get position of a chain
	 */
	private void getPosition() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				showProgress("Getting position...");
				// get chain name
				String name = (String) spChainNames.getSelectedItem();
				// get space
				String spaceString = (String) spSpace.getSelectedItem();
				int space = RobotMotionCartesianController.FRAME_ROBOT;
				if (spaceString.equalsIgnoreCase("FRAME_WORLD")) {
					space = RobotMotionCartesianController.FRAME_WORLD;
				} else if (spaceString.equalsIgnoreCase("FRAME_TORSO")) {
					space = RobotMotionCartesianController.FRAME_TORSO;
				}
				try {
					// get position of a chain
					final RobotPosition6D position = RobotMotionCartesianController
							.getPosition(getRobot(), name, space, true);
					if (position == null) {
						showInfoDialogFromThread("MotionOthers",
								"Get position failed: ");
					} else {
						// display position
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								tvPosition.setText("x: " + position.x + " y: "
										+ position.y + " z: " + position.z
										+ " wx: " + position.wx + " wy: "
										+ position.wy + " wz: " + position.wz);
							}
						});
					}
				} catch (RobotException e) {
					cancelProgress();
					showInfoDialogFromThread("MotionOthers",
							"Get position failed: " + e.getMessage());
					e.printStackTrace();
				}
				cancelProgress();
			}
		}).start();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.robot_api_demo_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			showInfoDialog(TAG, INSTRUCTIONS);
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	protected int getLayoutID() {
		return R.layout.activity_motion_set_position;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btSetPosition:
			setPosition();
			break;
		case R.id.btGetPosition:
			getPosition();
			break;
		default:
			break;
		}

	}

}
