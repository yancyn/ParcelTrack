![logo](https://raw.githubusercontent.com/yancyn/ParcelTrack/master/ParcelTrack/src/main/res/drawable-mdpi/parcel.png) ParcelTrack
===========
Track on Malaysia carries parcel like Poslaju, CityLink, GDex, Skynet, and FedEx.

Features
---------
1. Trace by parcel number.
2. Keep history.
3. Arrival indicator: Red, Yellow, and Green for delivered.
4. Only support Poslaju, Citylink, GDex, Skynet, and FedEx temporarily.

Code Declarations
-------------------
Initialize
---------
	ShipmentManager manager = new ShipmentManager();

New search
------------
	1. manager.track(consignmentNo);
	2. or manager.track(Carrier, consignmentNo);

Update case
------------
	manager.refresh(index, consignmentNo);
	
Update All
-----------
	manager.refreshAll();
	
Delete case
------------
	manager.delete(index, consignmentNo);
	
Delete All
----------
	manager.deleteAll();
	
	

References
----------
- http://www.pos.com.my/emstrack/viewdetail.asp?parcelno=EF216286916MY
- http://intranet.gdexpress.com/official/etracking.php?capture=4340560475&Submit=Track
- https://www.fedex.com/fedextrack/index.html?tracknumbers=797337230186
