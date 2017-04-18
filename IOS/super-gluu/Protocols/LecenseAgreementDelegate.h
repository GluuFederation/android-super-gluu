//
//  LecenseAgreementDelegate.h
//  super-gluu
//
//  Created by Nazar Yavornytskyy on 3/3/16.
//  Copyright © 2016 Gluu. All rights reserved.
//

@protocol ApproveDenyDelegate

-(void)approveRequest;
-(void)denyRequest;
-(void)openRequest;

@end